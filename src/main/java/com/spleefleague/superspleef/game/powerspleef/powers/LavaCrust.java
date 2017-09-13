package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author balsfull
 */
public class LavaCrust extends Power {
    
    private BukkitTask task;
    
    private LavaCrust(SpleefPlayer sp) {
        super(PowerType.LAVA_CRUST, sp, 20 * 10);
    }

    @Override
    public boolean execute() {
        SpleefBattle battle = getPlayer().getCurrentBattle();
        Collection<FakeBlock> fieldBlocks = battle.getFieldBlocks();
        double maxDistanceSquared = 0;
        Collection<Location> fakeBlockLocations = fieldBlocks
                .stream()
                .filter(fb -> fb.getType() != Material.AIR)
                .map(fb -> fb.getLocation())
                .collect(Collectors.toSet());
        for(Location block : fakeBlockLocations) {
            maxDistanceSquared = Math.min(maxDistanceSquared, block.distanceSquared(getPlayer().getLocation()));
        }
        Optional<Location> target = getPlayer()
                .getLineOfSight(null, (int)Math.ceil(maxDistanceSquared))
                .stream()
                .map(b -> b.getLocation())
                .filter(l -> fakeBlockLocations.contains(l))
                .findFirst();
        if(!target.isPresent()) {
            return false;
        }
        int radius = 5;
        int duration = 20 * 3;
        double probability = 0.8;
        
        Collection<FakeBlock> areaAround = fieldBlocks
                .stream()
                .filter(fb -> fb.getType() != Material.AIR)
                .filter(fb -> fb.getLocation().distanceSquared(target.get()) <= radius*radius)
                .filter(fb -> Math.random() < probability)
                .collect(Collectors.toSet());
        areaAround.forEach(fb -> fb.setType(Material.OBSIDIAN));
        task = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            areaAround.forEach(fb -> fb.setType(Material.AIR));
        }, duration);
        return true;
    }

    @Override
    public void cancel() {
        if(task != null) {
            task.cancel();
        }
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new LavaCrust(sp);
    }
}
