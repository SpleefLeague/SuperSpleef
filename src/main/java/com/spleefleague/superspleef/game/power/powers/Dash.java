/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.core.utils.scheduler.PredicateScheduler;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.power.ChargePower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public class Dash extends ChargePower {
    private static final double STRENGTH = 1.3;
    private static final float MAX_PITCH = -10;
    private static final float MIN_PITCH = -20;
    private static final float AIR_DAMPENING_FACTOR = 0.7f;
    
    private Dash(SpleefPlayer sp, int cooldown, int maxCharges, int rechargeDelay) {
        super(sp, PowerType.DASH, cooldown, maxCharges, rechargeDelay);
    }

    @Override
    public void execute() {
        Player p = getPlayer();
        Location loc = getPlayer().getLocation().clone();
        loc.setPitch(Math.max(MIN_PITCH, Math.min(loc.getPitch(), MAX_PITCH)));
        Vector direction = loc.getDirection().normalize().multiply(STRENGTH);
        if(!VirtualWorld.getInstance().isOnGround(p)) {
            direction = direction.multiply(AIR_DAMPENING_FACTOR);
        }
        p.setVelocity(direction);
        FakeWorld fw = getBattle().getFakeWorld();
        fw.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.2f);
        PredicateScheduler.runTaskTimer(SuperSpleef.getInstance(), () -> {
            fw.spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation().add(0, 0.5, 0), 1, 0, 0, 0, 0);
        }, 0, 1, 10);
        
    }
    
    public static Function<SpleefPlayer, Dash> getSupplier() {
        return sp -> new Dash(sp, 40, 3, 400);
    }
}
