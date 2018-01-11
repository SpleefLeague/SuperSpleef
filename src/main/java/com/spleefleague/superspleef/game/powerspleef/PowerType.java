package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.EyeOfTheStorm;
import com.spleefleague.superspleef.game.powerspleef.powers.HeatBolts;
import com.spleefleague.superspleef.game.powerspleef.powers.LavaCrust;
import com.spleefleague.superspleef.game.powerspleef.powers.QuickStep;
import com.spleefleague.superspleef.game.powerspleef.powers.RollerSpades;
import com.spleefleague.superspleef.game.powerspleef.powers.RunnerShoes;
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

    NO_POWER("Disable power", Material.BARRIER, Power.emptyPower()),
    ROLLER_SPADES("Roller Spades", Material.DIAMOND_SPADE, 0, 1560, RollerSpades.getSupplier()),
    QUICK_STEP("Quick Step", Material.DIAMOND_SPADE, 0, 1559, QuickStep.getSupplier()),
    LAVA_CRUST("Lava Crust", Material.DIAMOND_SPADE, 0, 1558, LavaCrust.getSupplier()),
    //EYE_OF_THE_STORM("Eye of the Storm", Material.DIAMOND_SPADE, 0, 1557, EyeOfTheStorm.getSupplier()),
    HEAT_BOLTS("Heat Bolts", Material.DIAMOND_SPADE, 0, 1556, HeatBolts.getSupplier()),
    RUNNER_SHOES("Runner Shoes", Material.DIAMOND_SPADE, 0, 1555, RunnerShoes.getSupplier());
    
    private final String name;
    private final Material type;
    private final byte data;
    private final short damage;
    private final Function<SpleefPlayer, ? extends Power> powerSupplier;
    
    private PowerType(String name, Material type, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this(name, type, (byte)0, powerSupplier);
    }
    
    private PowerType(String name, Material type, int data, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this(name, type, data, 0, powerSupplier);
    }
    
    private PowerType(String name, Material type, int data, int damage, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this.name = name;
        this.type = type;
        this.data = (byte)(data & 0xFF);
        this.damage = (short)(damage & 0xFFFF);
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
    
    public short getDamage() {
        return damage;
    }
    
    public static Collection<PowerType> defaultPowers;
    
    static {
        defaultPowers = Collections.unmodifiableCollection(Arrays.asList(new PowerType[]{
            ROLLER_SPADES,
            QUICK_STEP,
            HEAT_BOLTS
        }));
    }
}
