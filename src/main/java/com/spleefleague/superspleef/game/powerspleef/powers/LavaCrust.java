package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.fakeblocks.packet.FakeArea;
import com.spleefleague.fakeblocks.packet.FakeBlock;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
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
        FakeArea area = battle.getField();
        double maxDistanceSquared = 0;
        Collection<Location> fakeBlockLocations = area
                .getBlocks()
                .stream()
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
        int radius = 7;
        int duration = 20 * 3;
        double probability = 0.5;
        
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
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new LavaCrust(sp);
    }
}
