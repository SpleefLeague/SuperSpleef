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
        Location loc = getPlayer().getLocation().subtract(0, 1, 0).getBlock().getLocation();
        Optional<FakeBlock> target = getPlayer()
                .getCurrentBattle()
                .getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() != Material.AIR)
                .filter(fb -> (fb.getLocation().equals(loc)))
                .findAny();
        if(!target.isPresent()) {
            return false;
        }
        int radius = 7;
        int duration = 20 * 3;
        double probability = 0.5;
        
        Collection<FakeBlock> areaAround = getPlayer().getCurrentBattle().getFieldBlocks()
                .stream()
                .filter(fb -> fb.getType() != Material.AIR)
                .filter(fb -> fb.getLocation().distanceSquared(target.get().getLocation()) <= radius*radius)
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

    @Override
    public void destroy() {
        cancel();
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new LavaCrust(sp);
    }
}
