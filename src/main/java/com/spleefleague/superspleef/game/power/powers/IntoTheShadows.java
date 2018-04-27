/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.power.ChargePower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.v1_12_R1.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author jonas
 */
public class IntoTheShadows extends ChargePower {

    private final int duration;
    private BukkitTask activeTask;
    
    private IntoTheShadows(SpleefPlayer sp, int cooldown, int maxCharges, int refillDelay, int duration) {
        super(sp, PowerType.INTO_THE_SHADOWS, cooldown, maxCharges, refillDelay);
        this.duration = duration;
    }

    @Override
    public void execute() {
        if(activeTask != null) {
            activeTask.cancel();
        }
        SpleefPlayer player = getPlayer();
        FakeWorld fw = getBattle().getFakeWorld();
        fw.playSound(player.getLocation(), Sound.ENTITY_ILLUSION_ILLAGER_MIRROR_MOVE, 1.0f, 0.5f);
        this.showDuration("Invisible", duration);
        Stream.of(
                getBattle().getActivePlayers().stream(), 
                getBattle().getSpectators().stream())
                .flatMap(Function.identity())
                .filter(sp -> sp != player)
                .forEach(sp -> {
                    sp.hidePlayer(player.getPlayer());
                });
        activeTask = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            resetTask();
        }, duration);
    }

    @Override
    public void cleanupRound() {
        super.cleanupRound();
        resetTask();
    }
    
    private void resetTask() {
        if(activeTask != null) {
            activeTask.cancel();
            activeTask = null;
            SpleefBattle<?> battle = getBattle();
            if(battle != null) {
                Stream.of(
                        getBattle().getActivePlayers().stream(), 
                        getBattle().getSpectators().stream())
                        .flatMap(Function.identity())
                        .filter(sp -> sp != getPlayer())
                        .forEach(sp -> {
                            sp.showPlayer(getPlayer().getPlayer());
                        });
            }
            PlayerUtil.actionbar(getPlayer(), new ChatMessage(""));
        }
    }
    
    public static Function<SpleefPlayer, IntoTheShadows> getSupplier() {
        return sp -> new IntoTheShadows(sp, 30, 3, 400, 20);
    }
}
