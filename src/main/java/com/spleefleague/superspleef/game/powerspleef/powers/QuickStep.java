package com.spleefleague.superspleef.game.powerspleef.powers;

import com.spleefleague.superspleef.game.powerspleef.Power;
import com.spleefleague.superspleef.game.powerspleef.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Supplier;
import org.bukkit.util.Vector;

/**
 *
 * @author balsfull
 */
public class QuickStep extends Power {
    
    private final double STRENGTH = 1;
    
    private QuickStep() {
        super(PowerType.QUICK_STEP, 20 * 5);
    }

    @Override
    public boolean execute(SpleefPlayer player) {
        Vector direction = player.getLocation().getDirection().normalize().multiply(STRENGTH);
        player.setVelocity(direction);
        return true;
    }
    
    public static Supplier<QuickStep> getSupplier() {
        return () -> new QuickStep();
    }

    @Override
    public void cancel() {
    
    }
}
