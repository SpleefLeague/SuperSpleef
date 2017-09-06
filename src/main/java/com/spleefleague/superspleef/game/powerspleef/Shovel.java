package com.spleefleague.superspleef.game.powerspleef;

import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public enum Shovel {

    DIAMOND("Diamond Shovel", Material.DIAMOND_SPADE, Material.DIAMOND_SPADE),
    GOLD("Golden Shovel", Material.GOLD_SPADE, Material.GOLD_SPADE, PowerType.LAVA_CRUST);

    private final String name;
    private final Material icon, ingame;
    private final byte data;
    private final PowerType[] uniquePowers;
    
    private Shovel(String name, Material ingame, Material icon, PowerType... unique) {
        this(name, ingame, icon, (byte)0, unique);
    }
    
    private Shovel(String name, Material ingame, Material icon, byte data, PowerType... unique) {
        this.name = name;
        this.ingame = ingame;
        this.icon = icon;
        this.data = data;
        this.uniquePowers = unique;
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
     * @return the uniquePowers
     */
    public PowerType[] getUniquePowers() {
        return uniquePowers;
    }
}
