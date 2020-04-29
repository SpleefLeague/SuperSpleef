/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.listener;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.Field;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.power.PowerSpleefBattle;
import com.spleefleague.superspleef.game.team.TeamSpleefArena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.event.FakeBlockBreakEvent;
import com.spleefleague.virtualworld.event.FakeBlockPlaceEvent;
import java.util.Arrays;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
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
        if (sp != null) {
            if (sp.isFrozen()) {
                Location spawn = sp.getCurrentBattle().getData(sp).getSpawn();
                if (spawn.distanceSquared(sp.getLocation()) > 2) {
                    sp.teleport(spawn);
                }
            } else if (!sp.isIngame()) {
                SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
                if (slp != null && !(slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER)) {
                    for (Arena arena : Arena.getAll()) {
                        if (arena.isTpBackSpectators() && arena.getBorder().isInArea(sp.getLocation())) {
                            Location loc = arena.getSpectatorSpawn();
                            if (loc == null) {
                                loc = SpleefLeague.getInstance().getSpawnManager().getNext().getLocation();
                            }
                            sp.teleport(loc);
                            break;
                        }
                    }
                    for (TeamSpleefArena arena : TeamSpleefArena.getAll()) {
                        if (arena.isTpBackSpectators() && arena.getBorder().isInArea(sp.getLocation())) {
                            Location loc = arena.getSpectatorSpawn();
                            if (loc == null) {
                                loc = SpleefLeague.getInstance().getSpawnManager().getNext().getLocation();
                            }
                            sp.teleport(loc);
                            break;
                        }
                    }
                }
            } else {
                SpleefBattle battle = sp.getCurrentBattle();
                Arena arena = battle.getArena();
                if (event.getPlayer().getGameMode() != GameMode.SPECTATOR
                        && !battle.isInCountdown()
                        && (PlayerUtil.isInLava(event.getPlayer())
                        || PlayerUtil.isInWater(event.getPlayer())
                        || !arena.getBorder().isInArea(sp.getLocation()))) {
                    battle.onArenaLeave(sp);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if (sp.isIngame()) {
            event.setCancelled(event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.SNOW_BLOCK);
            SpleefBattle battle = sp.getCurrentBattle();
            if(battle instanceof PowerSpleefBattle && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                ((PowerSpleefBattle)battle).requestPowerUse(sp);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFakeBlockBreak(FakeBlockBreakEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if(slp.getState().equals(PlayerState.SPECTATING)) {
            event.setCancelled(true);
        } else if(slp.getState().equals(PlayerState.INGAME)) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
            boolean isDefaultSnow = false;
            for(Field field : Field.getDefaultFields()) {
                if(field.getDefaultWorld() == event.getBlock().getWorld()) {
                    isDefaultSnow = true;
                    break;
                }
            }
            if (isDefaultSnow) {
                event.setCancelled(true);
            }
            else {
                Optional<? extends SpleefBattle> battle = Arrays
                        .stream(SuperSpleef.getInstance().getBattleManagers())
                        .flatMap(b -> b.getAll().stream())
                        .filter(b -> b.getFakeWorld() == event.getBlock().getWorld())
                        .findAny();
                if(battle.isPresent()) {
                    if(sp.isIngame() && sp.getCurrentBattle() == battle.get()) {
                        event.setCancelled(battle.get().isInCountdown());
                    }
                    else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if(slp.getState().equals(PlayerState.SPECTATING)) {
            event.setCancelled(true);
        }
    }
    
    public void onSlotChange(PlayerItemHeldEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if (slp.getState().equals(PlayerState.INGAME)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFakeBlockPlace(FakeBlockPlaceEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if(slp.getState().equals(PlayerState.SPECTATING)) {
            event.setCancelled(true);
        } else if(slp.getState().equals(PlayerState.INGAME)) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
            FakeWorld target = event.getBlock().getWorld();
            for(Field field : Field.getDefaultFields()) {
                if(field.getDefaultWorld() == target) {
                    event.setCancelled(true);
                    return;
                }
            }
            if(sp.isIngame()) {
                if(sp.getCurrentBattle().getFakeWorld() == target) {
                    event.setCancelled(true);
                }
            }
            else {
                boolean isIngame = Arrays.stream(SuperSpleef.getInstance().getBattleManagers())
                        .flatMap(bm -> bm.getAll().stream())
                        .map(battle -> battle.getFakeWorld())
                        .filter(fw -> fw == target)
                        .findAny()
                        .isPresent();
                if(isIngame) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(event.getPlayer());
        if(slp.getState().equals(PlayerState.SPECTATING)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(event.getPlayer());
        if (sp.isIngame()) {
            event.setCancelled(event.getItemDrop().getItemStack().getType() == Material.DIAMOND_SHOVEL);
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get((Player) event.getWhoClicked());
            if (sp.isIngame()) {
                event.setCancelled(true);
            }
        }
    }
}
