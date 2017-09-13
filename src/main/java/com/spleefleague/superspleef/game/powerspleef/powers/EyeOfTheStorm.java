package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.spleefleague.virtualworld.api.FakeBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class EyeOfTheStorm extends Power {
    
    private BukkitTask task;
    private Collection<FakeBlock> iceCage;
    
    private EyeOfTheStorm(SpleefPlayer sp) {
        super(PowerType.EYE_OF_THE_STORM, sp, 20 * 15);
    }

    @Override
    public boolean execute() {
        SpleefBattle battle = getPlayer().getCurrentBattle();
        Collection<Vector> cageDefinition = battle.getCageDefinition();
        iceCage = new ArrayList<>();
        for(Vector v : cageDefinition) {
            FakeBlock fb = battle.getFakeWorld().getBlockAt(getPlayer().getLocation().clone().add(v));
            if(fb != null && fb.getType() == Material.AIR) {
                iceCage.add(fb);
                fb.setType(Material.ICE);
            }
        }
        task = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            iceCage.forEach(fb -> fb.setType(Material.AIR));
        }, 3 * 20);
        return true;
    }

    @Override
    public void cancel() {
        if(task != null) {
            task.cancel();
            iceCage.forEach(fb -> fb.setType(Material.AIR));
        }
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new EyeOfTheStorm(sp);
    }
}
