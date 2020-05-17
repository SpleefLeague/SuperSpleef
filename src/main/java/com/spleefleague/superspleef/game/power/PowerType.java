package com.spleefleague.superspleef.game.power;

import com.spleefleague.superspleef.game.power.powers.IntoTheShadows;
import com.spleefleague.superspleef.game.power.powers.Invisibility;
import com.spleefleague.superspleef.game.power.powers.Dash;
import com.spleefleague.superspleef.game.power.powers.Nuke;
import com.spleefleague.superspleef.game.power.powers.Regenerate;
import com.spleefleague.superspleef.game.power.powers.SpeedBoost;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author balsfull
 */
public enum PowerType {

    EMPTY_POWER("No power", new String[]{
        ChatColor.GOLD + "No power selected"
    }, Material.BARRIER, Power.emptyPower()),
    
    INVISIBILITY("Frost Cloak", new String[]{
        ChatColor.GOLD + "You vanish from your opponent's sight,",
        ChatColor.GOLD + "you become visible if you break a block",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "20s",
        ChatColor.AQUA + "Duration: " + ChatColor.GRAY + "5s",
    }, Material.DIAMOND_AXE, 303, Invisibility.getSupplier()),
    
    DASH("Air Dash", new String[]{
        ChatColor.GOLD + "Quickly dashes you forward",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "2s",
        ChatColor.AQUA + "Charges: " + ChatColor.GRAY + "3",
        ChatColor.AQUA + "Recharge Delay: " + ChatColor.GRAY + "20s"
    }, Material.DIAMOND_AXE, 301, Dash.getSupplier()),
    
    NUKE("Melting Burst", new String[]{
        ChatColor.GOLD + "Destroys the blocks surrounding you",
        ChatColor.GOLD + "after a short delay. The blocks ",
        ChatColor.GOLD + "regenerate right afterwards",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "30s",
        ChatColor.AQUA + "Delay: " + ChatColor.GRAY + "1.5s"
    }, Material.DIAMOND_AXE, 304, Nuke.getSupplier()),
    
    REGENERATE("Regeneration", new String[]{
        ChatColor.GOLD + "Regenerates the blocks around you",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "17.5s",
    }, Material.DIAMOND_AXE, 305, Regenerate.getSupplier()),
    
    SPEED_BOOST("Speedboost", new String[]{
        ChatColor.GOLD + "Gives you a speedboost. Activate",
        ChatColor.GOLD + "again to increase the speed, and",
        ChatColor.GOLD + "shorten its duration",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "17.5s",
        ChatColor.AQUA + "Duration: " + ChatColor.GRAY + "10s",
    }, Material.DIAMOND_AXE, 306, SpeedBoost.getSupplier()),
    
    INTO_THE_SHADOWS("Into the Shadows", new String[]{
        ChatColor.GOLD + "Conceals you from your opponent",
        ChatColor.AQUA + "Cooldown: " + ChatColor.GRAY + "1.5s",
        ChatColor.AQUA + "Duration: " + ChatColor.GRAY + "1s",
        ChatColor.AQUA + "Charges: " + ChatColor.GRAY + "3",
        ChatColor.AQUA + "Recharge Delay: " + ChatColor.GRAY + "20s"
    }, Material.DIAMOND_AXE, 302, IntoTheShadows.getSupplier());
    
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
