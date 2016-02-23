/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.events.FakeBlockBreakEvent;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.Battle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Jonas
 */
public class GameListener implements Listener {

    private static Listener instance;
    
    public static void init() {
        if (instance == null) {
            instance = new GameListener();
            Bukkit.getPluginManager().registerEvents(instance, SuperSpleef.getInstance());
        }
    }
    
    private GameListener() {
        
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp != null) {
            if(sp.isFrozen()) {
                Location spawn = sp.getCurrentBattle().getData(sp).getSpawn();
                if(spawn.distanceSquared(sp.getLocation()) > 2) {
                    sp.teleport(spawn);
                }
            }
            else if (!sp.isIngame()) {
                SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
                if(!(slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER)) {
                    for (Arena arena : Arena.getAll()) {
                        if (arena.isTpBackSpectators() && arena.getBorder().isInArea(sp.getLocation())) {
                            Location loc = arena.getSpectatorSpawn();
                            if (loc == null) {
                                loc = SpleefLeague.getInstance().getSpawnLocation();
                            }
                            sp.teleport(loc);
                            break;
                        }
                    }
                }
            }
            else {
                Battle battle = SuperSpleef.getInstance().getBattleManager().getBattle(sp);
                Arena arena = battle.getArena();
                if(!arena.getBorder().isInArea(sp.getLocation()) || PlayerUtil.isInLava(event.getPlayer()) || PlayerUtil.isInWater(event.getPlayer())) {
                    battle.onArenaLeave(sp);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            event.setCancelled(event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.SNOW_BLOCK);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(FakeBlockBreakEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            if(sp.getCurrentBattle().isInCountdown()) {
                event.setCancelled(true);
            } else {
                event.setCancelled(!strongContains(sp.getCurrentBattle().getField().getBlocks(), event.getBlock()));
            }
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            event.setCancelled(event.getItemDrop().getItemStack().getType() == Material.DIAMOND_SPADE);
        }
    }
    
    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    
    private <T> boolean strongContains(Collection<T> col, T object) {
        for(T t : col) {
            if(t == object) {
                return true;
            }
        }
        return false;
    }
}