/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.power.CooldownPower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeWorld;
import com.spleefleague.virtualworld.event.FakeBlockBreakEvent;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.v1_12_R1.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author jonas
 */
public class Invisibility extends CooldownPower implements Listener {

    private final int duration;
    private BukkitTask activeTask;
    
    private Invisibility(SpleefPlayer sp, int cooldown, int duration) {
        super(sp, PowerType.INVISIBILITY, cooldown);
        this.duration = duration;
    }

    @Override
    public void execute() {
        if(activeTask != null) {
            activeTask.cancel();
        }
        SpleefPlayer player = getPlayer();
        FakeWorld fw = getBattle().getFakeWorld();
        fw.playSound(player.getLocation(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1.0f, 0.5f);
        this.showDuration("Invisible", duration);
        Stream.of(
                getBattle().getActivePlayers().stream(), 
                getBattle().getSpectators().stream())
                .flatMap(Function.identity())
                .filter(sp -> sp != player)
                .forEach(sp -> {
                    sp.hidePlayer(player.getPlayer());
                });
        Bukkit.getPluginManager().registerEvents(this, SuperSpleef.getInstance());
        activeTask = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            resetTask();
        }, duration);
    }

    @Override
    public void cleanupRound() {
        super.cleanupRound();
        resetTask();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        resetTask();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFakeBreak(FakeBlockBreakEvent event) {
        if(event.getPlayer().getUniqueId().equals(getPlayer().getUniqueId())) {
            resetTask();
        }
    }
    
    private void resetTask() {
        if(activeTask != null) {
            activeTask.cancel();
            activeTask = null;
            SpleefBattle<?> battle = getBattle();
            if(battle != null) {
                FakeBlockBreakEvent.getHandlerList().unregister(this);
                Stream.of(
                        battle.getActivePlayers().stream(), 
                        battle.getSpectators().stream())
                        .flatMap(Function.identity())
                        .filter(sp -> sp != getPlayer())
                        .forEach(sp -> {
                            sp.showPlayer(getPlayer().getPlayer());
                        });
                cancelDuration();
            }
            PlayerUtil.actionbar(getPlayer(), new ChatMessage(""));
        }
    }
    
    public static Function<SpleefPlayer, Invisibility> getSupplier() {
        return sp -> new Invisibility(sp, 400, 100);
    }
}
