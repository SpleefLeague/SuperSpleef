package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.powerspleef.powers.IntoTheShadows;
import com.spleefleague.superspleef.game.powerspleef.powers.Invisibility;
import com.spleefleague.superspleef.game.powerspleef.powers.Dash;
import com.spleefleague.superspleef.game.powerspleef.powers.Nuke;
import com.spleefleague.superspleef.game.powerspleef.powers.Regenerate;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author balsfull
 */
public enum PowerType {

    EMPTY_POWER("NO POWER", new String[]{
        "No power selected"
    }, Material.BARRIER, Power.emptyPower()),
    INVISIBILITY("Invisibility", new String[]{
        "Turn yourself invisible.",
        "Cannot spleef while",
        "invisible!"
    }, Material.DIAMOND_SPADE, 1560, Invisibility.getSupplier()),
    DASH("Dash", new String[]{
        "No power selected"
    }, Material.DIAMOND_SPADE, 1560, Dash.getSupplier()),
    NUKE("Nuke", new String[]{
        "No power selected"
    }, Material.DIAMOND_SPADE, 1560, Nuke.getSupplier()),
    REGENERATE("Regenerate", new String[]{
        "No power selected"
    }, Material.DIAMOND_SPADE, 1560, Regenerate.getSupplier()),
    INTO_THE_SHADOWS("Into the Shadows", new String[]{
        "No power selected"
    }, Material.DIAMOND_SPADE, 1559, IntoTheShadows.getSupplier());
    
    private final String name;
    private final List<String> description;
    private final Material type;
    private final short damage;
    private final Function<SpleefPlayer, ? extends Power> powerSupplier;
    
    private PowerType(String name, String[] description, Material type, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this(name, description, type, 0, powerSupplier);
    }
    
    private PowerType(String name, String[] description, Material type, int damage, Function<SpleefPlayer, ? extends Power> powerSupplier) {
        this.name = name;
        this.type = type;
        this.damage = (short)(damage & 0xFFFF);
        this.powerSupplier = powerSupplier;
        this.description = Arrays.asList(description);
    }
    
    public Power createPower(SpleefPlayer sp) {
        return powerSupplier.apply(sp);
    }
    
    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(type, 1, damage);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(description);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    
    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public String getDisplayName() {
        return name;
    }
    
    public Material getType() {
        return type;
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
