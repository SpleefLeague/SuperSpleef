/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;

/**
 *
 * @author jonas
 */
public abstract class CooldownPower extends Power {
    
    private final int cooldown;
    private int currentCooldown;
    
    public CooldownPower(SpleefPlayer sp, PowerType powerType, int cooldown) {
        super(sp, powerType);
        this.cooldown = cooldown;
    }
    
    @Override
    public void initRound() {
        super.initRound();
        unsetCooldown();
    }
    
    @Override
    public void cleanupRound() {
        super.cleanupRound();
        unsetCooldown();
    }
    
    @Override
    public boolean tryExecute() {
        if(currentCooldown > 0) {
            return false;
        }
        getPlayer().setExp(0);
        currentCooldown = cooldown;
        onCooldown.add(this);
        execute();
        return true;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        onCooldown.remove(this);
        getPlayer().setExp(0);
        getPlayer().setLevel(0);
    }
    
    protected void unsetCooldown() {
        onCooldown.remove(this);
        currentCooldown = 0;
        getPlayer().setLevel(0);
        getPlayer().setExp(1);
    }
    
    public abstract void execute();
    
    private static final List<CooldownPower> onCooldown;
    
    static {
        onCooldown = new LinkedList<>();
    }
    
    public static void startSchedulers() {
        Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            Iterator<CooldownPower> iter = onCooldown.iterator();
            while(iter.hasNext()) {
                CooldownPower cp = iter.next();
                cp.currentCooldown--;
                float percentage = (float)(cp.cooldown - cp.currentCooldown) / (float)cp.cooldown;
                cp.getPlayer().setExp(percentage);
                if(cp.currentCooldown <= 0) {
                    iter.remove();
                }
            }
        }, 0, 1);
    }
}
