/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;



/**
 *
 * @author Jonas
 */
public class EnvironmentListener implements Listener {
    
    private static Listener instance;
    
    public static void init() {
        if(instance == null) {
            instance = new EnvironmentListener();
            Bukkit.getPluginManager().registerEvents(instance, SuperSpleef.getInstance());
        }
    }
    
    private EnvironmentListener() {
        
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer()).getState() == PlayerState.IDLE) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
            for(Arena arena : Arena.getAll()) {
                if(!sp.getVisitedArenas().contains(arena)) {
                    if(arena.getArea().isInArea(event.getTo())) {
                        sp.getVisitedArenas().add(arena);
                        String title = ChatColor.GREEN + "You have discovered " + ChatColor.RED + arena.getName() + ChatColor.GREEN + "!";
                        String subtitle = ChatColor.GRAY + String.valueOf(sp.getVisitedArenas().size()) + "/" + String.valueOf(Arena.getAll().size()) + ChatColor.GOLD + " Spleef arenas found!";
                        PlayerUtil.sendTitle(event.getPlayer(), title, subtitle, 10, 40, 10);
                        event.getPlayer().playSound(event.getTo(), Sound.FIREWORK_BLAST, 1, 0);
                        break;
                    }
                }
            }
        }
    }
}