/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.core.utils.scheduler.PredicateScheduler;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.CooldownPower;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.function.Function;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author jonas
 */
public class Dash extends CooldownPower {

    private static final double STRENGTH = 1.3;
    private static final float MAX_PITCH = -10;
    private static final float MIN_PITCH = -20;
    private static final float AIR_DAMPENING_FACTOR = 0.7f;
    
    private Dash(SpleefPlayer sp, int cooldown) {
        super(sp, PowerType.DASH, cooldown);
    }

    @Override
    public void execute() {
        Player p = getPlayer();
        Location loc = getPlayer().getLocation().clone();
        p.getLocation().setY(loc.getY() + 0.05);
        loc.setPitch(Math.max(MIN_PITCH, Math.min(loc.getPitch(), MAX_PITCH)));
        Vector direction = loc.getDirection().normalize().multiply(STRENGTH);
        if(!VirtualWorld.getInstance().isOnGround(p)) {
            direction = direction.multiply(AIR_DAMPENING_FACTOR);
        }
        p.setVelocity(direction);
        FakeWorld fw = getBattle().getFakeWorld();
        PredicateScheduler.runTaskTimer(SuperSpleef.getInstance(), () -> {
            fw.spawnParticle(Particle.FIREWORKS_SPARK, p.getLocation().subtract(0, 0.5, 0), 1, 0, 0, 0, 0);
        }, 0, 3, (i) -> {
            return i < 5 && !VirtualWorld.getInstance().isOnGround(p);
        });
        
    }
    
    public static Function<SpleefPlayer, Dash> getSupplier() {
        return sp -> new Dash(sp, 10);
    }
}
