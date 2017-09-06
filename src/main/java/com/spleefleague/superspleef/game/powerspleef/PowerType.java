package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.EyeOfTheStorm;
import com.spleefleague.superspleef.game.powerspleef.powers.LavaCrust;
import com.spleefleague.superspleef.game.powerspleef.powers.QuickStep;
import com.spleefleague.superspleef.game.powerspleef.powers.RollerSpades;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public enum PowerType {

    NO_POWER("No power", Material.BARRIER, Power.emptyPower()),
    ROLLER_SPADES("Roller Spades", Material.IRON_BOOTS, RollerSpades.getSupplier()),
    QUICK_STEP("Quick Step", Material.FEATHER, QuickStep.getSupplier()),
    LAVA_CRUST("Lava Crust", Material.MAGMA, LavaCrust.getSupplier()),
    EYE_OF_THE_STORM("Eye of the Storm", Material.ICE, EyeOfTheStorm.getSupplier());
    
    private final String name;
    private final Material type;
    private final byte data;
    private final Function<SpleefPlayer, ? extends Power> powerSupplier;
    
    private PowerType(String name, Material type, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this(name, type, (byte)0, powerSupplier);
    }
    
    private PowerType(String name, Material type, byte data, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.powerSupplier = powerSupplier;
    }
    
    public Power createPower(SpleefPlayer sp) {
        return powerSupplier.apply(sp);
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
