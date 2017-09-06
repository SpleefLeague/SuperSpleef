package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.LavaCrust;
import com.spleefleague.superspleef.game.powerspleef.powers.QuickStep;
import com.spleefleague.superspleef.game.powerspleef.powers.RollerSpades;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public enum PowerType {

    NO_POWER("No power", Material.BARRIER, Power.emptyPower()),
    ROLLER_SPADES("Roller Spades", Material.IRON_BOOTS, RollerSpades.getSupplier()),
    QUICK_STEP("Quick Step", Material.FEATHER, QuickStep.getSupplier()),
    LAVA_CRUST("Lava Crust", Material.MAGMA, LavaCrust.getSupplier());
    
    private final String name;
    private final Material type;
    private final byte data;
    private final Supplier<? extends Power> powerSupplier;
    
    private PowerType(String name, Material type, Supplier<? extends Power> powerSupplier) {
        this(name, type, (byte)0, powerSupplier);
    }
    
    private PowerType(String name, Material type, byte data, Supplier<? extends Power> powerSupplier) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.powerSupplier = powerSupplier;
    }
    
    public Power createPower() {
        return powerSupplier.get();
    }

    public String getDisplayName() {
        return name;
    }
    
    public Material getType() {
        return type;
    }

    public byte getData() {
        return data;
    }
    
    public static Collection<PowerType> defaultPowers;
    
    static {
        defaultPowers = Collections.unmodifiableCollection(Arrays.asList(new PowerType[]{
            ROLLER_SPADES,
            QUICK_STEP
        }));
    }
}
