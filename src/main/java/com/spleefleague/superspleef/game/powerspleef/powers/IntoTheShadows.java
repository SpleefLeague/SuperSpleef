/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.ChargePower;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
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
        System.out.println("Executing shadows");
        SpleefPlayer player = getPlayer();
        Stream.of(
                getBattle().getActivePlayers().stream(), 
                getBattle().getSpectators().stream())
                .flatMap(Function.identity())
                .filter(sp -> sp != player)
                .forEach(sp -> {
                    sp.hidePlayer(player.getPlayer());
                });
        activeTask = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            cleanupRound();
        }, duration);
    }

    @Override
    public void cleanupRound() {
        if(activeTask != null) {
            activeTask.cancel();
            activeTask = null;
            getBattle().setVisibility(getPlayer());
        }
    }
    
    public static Function<SpleefPlayer, IntoTheShadows> getSupplier() {
        return sp -> new IntoTheShadows(sp, 40, 5, 200, 20);
    }
}
