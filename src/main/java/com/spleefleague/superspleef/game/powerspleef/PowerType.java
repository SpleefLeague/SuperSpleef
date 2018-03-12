package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.IntoTheShadows;
import com.spleefleague.superspleef.game.powerspleef.powers.Invisibility;
import com.spleefleague.superspleef.game.powerspleef.powers.Dash;
import com.spleefleague.superspleef.game.powerspleef.powers.Nuke;
import com.spleefleague.superspleef.game.powerspleef.powers.Regenerate;
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

    EMPTY_POWER("NO POWER", Material.BARRIER, Power.emptyPower()),
    INVISIBILITY("Invisibility", Material.DIAMOND_SPADE, 0, 1560, Invisibility.getSupplier()),
    DASH("Dash", Material.DIAMOND_SPADE, 0, 1560, Dash.getSupplier()),
    NUKE("Nuke", Material.DIAMOND_SPADE, 0, 1560, Nuke.getSupplier()),
    REGENERATE("Regenerate", Material.DIAMOND_SPADE, 0, 1560, Regenerate.getSupplier()),
    INTO_THE_SHADOWS("Into the Shadows", Material.DIAMOND_SPADE, 0, 1559, IntoTheShadows.getSupplier());
    
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
            INVISIBILITY
        }));
    }
}
