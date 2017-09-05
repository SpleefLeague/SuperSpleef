package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.fakeblocks.representations.FakeArea;
import com.spleefleague.fakeblocks.representations.FakeBlock;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
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
    
    private LavaCrust() {
        super(PowerType.LAVA_CRUST, 20 * 10);
    }

    @Override
    public boolean execute(SpleefPlayer player) {
        SpleefBattle battle = player.getCurrentBattle();
        FakeArea area = battle.getField();
        double maxDistanceSquared = 0;
        Collection<Location> fakeBlockLocations = area
                .getBlocks()
                .stream()
                .map(fb -> fb.getLocation())
                .collect(Collectors.toSet());
        for(Location block : fakeBlockLocations) {
            maxDistanceSquared = Math.min(maxDistanceSquared, block.distanceSquared(player.getLocation()));
        }
        Optional<Location> target = player
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
        
        Collection<FakeBlock> areaAround = area
                .getBlocks()
                .stream()
                .filter(fb -> fb.getLocation().distanceSquared(target.get()) <= radius*radius)
                .filter(fb -> Math.random() < probability)
                .collect(Collectors.toSet());
        areaAround.forEach(fb -> fb.setType(Material.OBSIDIAN));
        SpleefLeague.getInstance().getFakeBlockHandler().update(area);
        task = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            areaAround.forEach(fb -> fb.setType(Material.AIR));
            SpleefLeague.getInstance().getFakeBlockHandler().update(area);
        }, duration);
        return true;
    }

    @Override
    public void cancel() {
        if(task != null) {
            task.cancel();
        }
    }
    
    public static Supplier<LavaCrust> getSupplier() {
        return () -> new LavaCrust();
    }
}
