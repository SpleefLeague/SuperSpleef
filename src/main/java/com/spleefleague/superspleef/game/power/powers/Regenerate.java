/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.superspleef.game.power.CooldownPower;
import com.spleefleague.superspleef.game.power.PowerSpleefBattle;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.Particle;

/**
 *
 * @author jonas
 */
public class Regenerate extends CooldownPower {

    private final int range = 4;
    
    public Regenerate(SpleefPlayer sp, int cooldown) {
        super(sp, PowerType.REGENERATE, cooldown);
    }

    @Override
    public void execute() {
        SpleefPlayer sp = getPlayer();
        PowerSpleefBattle battle = getBattle();
        FakeWorld fw = battle.getFakeWorld();
        battle.getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() == Material.AIR)
                .filter(fb -> fb
                        .getLocation()
                        .clone()
                        .distanceSquared(sp.getLocation().getBlock().getLocation()) <= range * range)
                .forEach(fb -> {
                    fb.setType(Material.SNOW_BLOCK);
                    int count = (int)(Math.random() * 5);
                    fw.spawnParticle(Particle.SNOW_SHOVEL, fb.getLocation().add(0.5, 0.5, 0.5), count, 0.5, 0.2, 0.5, 0);
                });
    }
    
    public static Function<SpleefPlayer, Regenerate> getSupplier() {
        return s -> new Regenerate(s, 350);
    }
}
