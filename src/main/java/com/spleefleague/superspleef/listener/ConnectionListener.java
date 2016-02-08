/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Jonas
 */
public class ConnectionListener implements Listener {
    
    private static Listener instance;
    
    public static void init() {
        if(instance == null) {
            instance = new ConnectionListener();
            Bukkit.getPluginManager().registerEvents(instance, SuperSpleef.getInstance());
        }
    }
    
    private ConnectionListener() {
        
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            SuperSpleef.getInstance().getBattleManager().getBattle(sp).removePlayer(sp, false);
        }
        else {
            SuperSpleef.getInstance().getBattleManager().dequeue(sp);
        }
    }
}
