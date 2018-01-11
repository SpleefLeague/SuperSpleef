package com.spleefleague.superspleef.game.powerspleef;

import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public enum Shovel {

    DIAMOND("Diamond Shovel", Material.DIAMOND_SPADE, Material.DIAMOND_SPADE),
    GOLD("Golden Shovel", Material.GOLD_SPADE, Material.GOLD_SPADE);

    private final String name;
    private final Material icon, ingame;
    private final byte data;
    private final short damage;
    
    private Shovel(String name, Material ingame, Material icon) {
        this(name, ingame, icon, (byte)0);
    }
    
    
    private Shovel(String name, Material ingame, Material icon, int data) {
        this(name, ingame, icon, data, 0);
    }
    
    private Shovel(String name, Material ingame, Material icon, int data, int damage) {
        this.name = name;
        this.ingame = ingame;
        this.icon = icon;
        this.data = (byte)data;
        this.damage = (short)damage;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the menu icon
     */
    public Material getIcon() {
        return icon;
    }

    /**
     * @return the type
     */
    public Material getType() {
        return ingame;
    }

    /**
     * @return the data
     */
    public byte getData() {
        return data;
    }

    /**
     * @return the durability
     */
    public short getDamage() {
        return damage;
    }
}
