/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author jonas
 */
public class HeatBolts extends Power {
    
    private static final int DESTROY_DELAY = 1;
    private BukkitTask destroyTask;
    
    private HeatBolts(SpleefPlayer player) {
        super(PowerType.HEAT_BOLTS, player, 1);
    }

    @Override
    public boolean execute() {
        SpleefBattle battle = getPlayer().getCurrentBattle();
        Collection<FakeBlock> fieldBlocks = battle.getFieldBlocks();
        Map<Location, FakeBlock> fakeBlockLocations = fieldBlocks
                .stream()
                .filter(fb -> fb.getType() != Material.AIR)
                .collect(Collectors.toMap(
                        FakeBlock::getLocation, 
                        Function.identity()
                ));
        double maxDistanceSquared = fakeBlockLocations.keySet()
                .stream()
                .mapToDouble(l -> l.distanceSquared(getPlayer().getLocation()))
                .max()
                .orElse(0);
        Iterator<FakeBlock> target = getPlayer()
                .getLineOfSight(null, (int)Math.ceil(maxDistanceSquared))
                .stream()
                .map(b -> b.getLocation())
                .filter(l -> fakeBlockLocations.containsKey(l))
                .map(l -> fakeBlockLocations.get(l))
                .collect(Collectors.toList()).iterator();
        destroyTask = Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            if(!target.hasNext()) {
                destroyTask.cancel();
            }
            else {
                target.next().setType(Material.AIR);
            }
        }, 0, DESTROY_DELAY);
        return true;
    }
    
    @Override
    public void cancel() {
        if(destroyTask != null) {
            destroyTask.cancel();
        }
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new HeatBolts(sp);
    }
}
