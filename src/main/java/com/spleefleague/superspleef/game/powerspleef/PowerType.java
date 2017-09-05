package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.LavaCrust;
import com.spleefleague.superspleef.game.powerspleef.powers.QuickStep;
import com.spleefleague.superspleef.game.powerspleef.powers.RollerSpades;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 *
 * @author balsfull
 */
public enum PowerType {

    ROLLER_SPADES("Roller Spades", RollerSpades.getSupplier()),
    QUICK_STEP("Quick Step", QuickStep.getSupplier()),
    LAVA_CRUST("Lava Crust", LavaCrust.getSupplier());
    
    private final String name;
    private final Supplier<? extends Power> powerSupplier;
    
    private PowerType(String name, Supplier<? extends Power> powerSupplier) {
        this.name = name;
        this.powerSupplier = powerSupplier;
    }
    
    public Power createPower() {
        return powerSupplier.get();
    }

    public String getDisplayName() {
        return name;
    }
    
    public static Collection<PowerType> defaultPowers;
    
    static {
        defaultPowers = Collections.unmodifiableCollection(Arrays.asList(new PowerType[]{
            ROLLER_SPADES,
            QUICK_STEP
        }));
    }
}
