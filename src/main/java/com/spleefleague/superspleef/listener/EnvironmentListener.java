/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.fakeblocks.packet.FakeBlockHandler;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.TeamSpleefArena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Jonas
 */
public class EnvironmentListener implements Listener {

    private static Listener instance;

    public static void init() {
        if (instance == null) {
            instance = new EnvironmentListener();
            Bukkit.getPluginManager().registerEvents(instance, SuperSpleef.getInstance());
        }
    }

    private EnvironmentListener() {

    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if (slp != null && slp.getState() == PlayerState.IDLE) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
            for (Arena arena : Arena.getAll()) {
                if (!sp.getVisitedArenas().contains(arena)) {
                    if (arena.getArea().isInArea(event.getTo())) {
                        sp.getVisitedArenas().add(arena);
                        if(!arena.isDefaultArena()) {
                            String title = ChatColor.GREEN + "You have discovered " + ChatColor.RED + arena.getName() + ChatColor.GREEN + "!";
                            String subtitle = ChatColor.GRAY + String.valueOf(sp.getVisitedArenas().size()) + "/" + String.valueOf(Arena.getAll().size()) + ChatColor.GOLD + " Spleef arenas found!";
                            PlayerUtil.sendTitle(event.getPlayer(), title, subtitle, 10, 40, 10);
                            event.getPlayer().playSound(event.getTo(), Sound.ENTITY_FIREWORK_BLAST, 1, 0);
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        FakeBlockHandler handler = SpleefLeague.getInstance().getFakeBlockHandler();
        for (Arena arena : Arena.getAll()) {
            handler.addArea(arena.getDefaultSnow(), false, event.getPlayer());
        }
        for (Arena arena : TeamSpleefArena.getAll()) {
            handler.addArea(arena.getDefaultSnow(), false, event.getPlayer());
        }
    }
}
