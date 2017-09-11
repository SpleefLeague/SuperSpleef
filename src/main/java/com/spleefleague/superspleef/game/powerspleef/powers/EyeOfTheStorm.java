package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.fakeblocks.packet.FakeArea;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author balsfull
 */
public class EyeOfTheStorm extends Power {
    
    private BukkitTask task;
    private FakeArea iceCage;
    
    private EyeOfTheStorm(SpleefPlayer sp) {
        super(PowerType.EYE_OF_THE_STORM, sp, 20 * 15);
    }

    @Override
    public boolean execute() {
        SpleefBattle battle = getPlayer().getCurrentBattle();
        iceCage = battle.getCageBlocks(getPlayer().getLocation(), Material.ICE);
        SpleefLeague.getInstance().getFakeBlockHandler().addArea(iceCage, true, battle.getActivePlayers().toArray(new SpleefPlayer[0]));
        SpleefLeague.getInstance().getFakeBlockHandler().addArea(iceCage, true, battle.getSpectators().toArray(new SpleefPlayer[0]));
        task = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            iceCage.getBlocks().forEach(fb -> fb.setType(Material.AIR));
            SpleefLeague.getInstance().getFakeBlockHandler().update(iceCage);
        }, 60);
        return true;
    }

    @Override
    public void cancel() {
        if(task != null) {
            task.cancel();
        }
    }
    
    public static Function<SpleefPlayer, ? extends Power> getSupplier() {
        return (sp) -> new EyeOfTheStorm(sp);
    }
}
