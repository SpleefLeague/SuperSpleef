/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.listener;

import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.chat.ChatManager;
import net.spleefleague.core.chat.Theme;
import net.spleefleague.core.events.BattleCancelEvent;
import net.spleefleague.core.events.PlayerDequeueEvent;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sp.isIngame()) {
            if (event.getBlock().getType() == Material.SNOW_BLOCK && !event.isCancelled()) {
                sp.getCurrentBattle().addDestroyedBlock(event.getBlock());
            }
            else {
                event.setCancelled(true);
            }
        } 
    }
    
    @EventHandler
    public void onLeaveRequest(PlayerDequeueEvent event) {
        if(!event.wasSuccessful()) {    
            SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer().getPlayer());
            SuperSpleef.getInstance().getBattleManager().dequeue(sjp);
            event.setSuccessful(true);
        }
    }
    
    @EventHandler
    public void onCancelRequest(BattleCancelEvent event) {
        if(!event.wasSuccessful()) {
            SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer().getPlayer());
            if(sjp.isIngame()) {
                Battle b = sjp.getCurrentBattle();
                b.cancel();
                event.setSuccessful(true);
                ChatManager.sendMessage(Theme.SUPER_SECRET + " The battle on " + b.getArena().getName() + " has been cancelled.", "STAFF");
            }
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sjp.isFrozen()) {
            Location from = event.getFrom();
            Location to = event.getTo();
            from.setY(to.getY());
            from.setYaw(to.getYaw());
            from.setPitch(to.getPitch());
            event.setTo(from);
        }
        else if(!sjp.isIngame()) {
            for(Arena arena : Arena.getAll()) {
                if(arena.getBorder().isInArea(sjp.getPlayer().getLocation())) {
                    Location loc = arena.getSpectatorSpawn();
                    if(loc == null) {
                        loc = SpleefLeague.DEFAULT_WORLD.getSpawnLocation();
                    }
                    sjp.getPlayer().teleport(loc);
                    break;
                }
            }
        }
        else {
            Battle battle = SuperSpleef.getInstance().getBattleManager().getBattle(sjp);
            Arena arena = battle.getArena();
            if(!arena.getBorder().isInArea(sjp.getPlayer().getLocation())) {
                battle.onArenaLeave(sjp);
            }
            else if((PlayerUtil.isInLava(event.getPlayer()) || PlayerUtil.isInWater(event.getPlayer()))) {
                battle.onArenaLeave(sjp);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sjp.isIngame()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sjp.isIngame()) {
            event.setCancelled(true);
        }
    }
    
    public void onDrop(PlayerDropItemEvent event) {
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if(sjp.isIngame()) {
            event.setCancelled(event.getItemDrop().getItemStack().getType() == Material.DIAMOND_SPADE);
        }
    }
}
