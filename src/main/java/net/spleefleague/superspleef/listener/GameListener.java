/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.listener;

import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.utils.PlayerUtil;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Arena;
import net.spleefleague.superspleef.game.Battle;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
        if(sp.isFrozen()) {
            Location spawn = sp.getCurrentBattle().getData(sp).getSpawn();
            if(spawn.distanceSquared(sp.getPlayer().getLocation()) > 2) {
                sp.getPlayer().teleport(spawn);
            }
        }
        if (!sp.isIngame()) {
            for (Arena arena : Arena.getAll()) {
                if (arena.isTpBackSpectators() && arena.getBorder().isInArea(sp.getPlayer().getLocation())) {
                    Location loc = arena.getSpectatorSpawn();
                    if (loc == null) {
                        loc = SpleefLeague.DEFAULT_WORLD.getSpawnLocation();
                    }
                    sp.getPlayer().teleport(loc);
                    break;
                }
            }
        }
        else {
            Battle battle = SuperSpleef.getInstance().getBattleManager().getBattle(sp);
            Arena arena = battle.getArena();
            if(!arena.getBorder().isInArea(sp.getPlayer().getLocation()) || PlayerUtil.isInLava(event.getPlayer()) || PlayerUtil.isInWater(event.getPlayer())) {
                battle.onArenaLeave(sp);
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
    public void onBlockBreak(BlockBreakEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            if(sp.getCurrentBattle().isInCountdown() || event.getBlock().getType() != Material.SNOW_BLOCK) {
                event.setCancelled(true);
            }
            else {
                event.setCancelled(false);
                sp.getCurrentBattle().addDestroyedBlock(event.getBlock());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            event.setCancelled(true);
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
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onDamage(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}