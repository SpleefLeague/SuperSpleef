/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.bukkit.Bukkit;

/**
 *
 * @author jonas
 */
public abstract class ChargePower extends Power {

    private final int cooldown, maxCharges, chargeRefillDelay;
    private int currentCooldown, charges, currentChargeRefillProgess;
    
    public ChargePower(SpleefPlayer sp, PowerType powerType, int cooldown, int maxCharges, int chargeRefillDelay) {
        super(sp, powerType);
        this.cooldown = cooldown;
        this.maxCharges = maxCharges;
        this.charges = maxCharges;
        this.chargeRefillDelay = chargeRefillDelay;
    }
    
    @Override
    public void initRound() {
        getPlayer().setExp(1);
        getPlayer().setLevel(charges);
    }
    
    @Override
    public boolean tryExecute() {
        if(currentCooldown > 0) {
            return false;
        }
        if(charges <= 0) {
            return false;
        }
        charges--;
        getPlayer().setLevel(charges);
        getPlayer().setExp(0);
        currentCooldown = cooldown;
        refilling.add(this);
        execute();
        return true;
    }
    
    public abstract void execute();
    
    private static final Collection<ChargePower> refilling;
    
    static {
        refilling = new HashSet<>();
    }
    
    public static void startSchedulers() {
        Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            Iterator<ChargePower> iter = refilling.iterator();
            while(iter.hasNext()) {
                ChargePower cp = iter.next();
                if(cp.currentCooldown > 0) {
                    cp.currentCooldown--;
                }
                if(cp.charges < cp.maxCharges) {
                    cp.currentChargeRefillProgess++;
                    if(cp.currentChargeRefillProgess >= cp.chargeRefillDelay) {
                        cp.currentChargeRefillProgess = 0;
                        cp.charges++;
                        cp.getPlayer().setLevel(cp.charges);
                    }
                }
                if(cp.charges > 0) {
                    float percentage = (float)(cp.cooldown - cp.currentCooldown) / (float)cp.cooldown;
                    cp.getPlayer().setExp(percentage);
                }
                else {
                    float percentage = (float)(cp.currentChargeRefillProgess - cp.chargeRefillDelay) / (float)cp.chargeRefillDelay;
                    cp.getPlayer().setExp(percentage);
                }
                if(cp.charges >= cp.maxCharges && cp.currentCooldown <= 0) {
                    iter.remove();
                }
            }
        }, 0, 1);
    }
}
