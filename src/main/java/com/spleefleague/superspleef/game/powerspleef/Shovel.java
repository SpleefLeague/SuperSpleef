package com.spleefleague.superspleef.game.powerspleef;

import org.bukkit.Material;

/**
 *
 * @author balsfull
 */
public enum Shovel {

    DIAMOND("Diamond Shovel", Material.DIAMOND_SPADE),
    GOLD("Golden Shovel", Material.GOLD_SPADE, PowerType.LAVA_CRUST);

    private final String name;
    private final Material type;
    private final byte data;
    private final PowerType[] uniquePowers;
    
    private Shovel(String name, Material type, PowerType... unique) {
        this(name, type, (byte)0, unique);
    }
    
    private Shovel(String name, Material type, byte data, PowerType... unique) {
        this.name = name;
        this.type = type;
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
     * @return the type
     */
    public Material getType() {
        return type;
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
