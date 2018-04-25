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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

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
        fw.playSound(sp.getLocation(), Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON, 1.0f, 2.0f);
        int lowest = battle.getFieldBlocks()
                .stream()
                .mapToInt(fb -> fb.getY())
                .min()
                .getAsInt();
        double height = lowest - (sp.getLocation().getY() - 1.2);
        height /= 2.0;
        if(height > 0) {
            sp.setVelocity(new Vector(0, Math.min(height, 0.6), 0));
        }
        battle.getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() == Material.AIR)
                .filter(fb -> {
                        Location fbl = fb.getLocation().clone();
                        fbl.setY(0);
                        Location spl = sp.getLocation().getBlock().getLocation();
                        spl.setY(0);
                        //Horizontal distance
                        double distance = fbl.distanceSquared(spl);
                        return distance <= range * range;
                }).forEach(fb -> {
                    fb.setType(Material.SNOW_BLOCK);
                    int count = (int)(Math.random() * 5);
                    fw.spawnParticle(Particle.SNOW_SHOVEL, fb.getLocation().add(0.5, 0.5, 0.5), count, 0.5, 0.2, 0.5, 0);
                });
    }
    
    public static Function<SpleefPlayer, Regenerate> getSupplier() {
        return s -> new Regenerate(s, 350);
    }
}
