/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.scoreboards;

import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSaveable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author Jonas
 */
public class BlockData extends DBEntity implements DBLoadable, DBSaveable {

    @DBLoad(fieldName = "material")
    private Material material;
    @DBLoad(fieldName = "data")
    private int data;

    public Material getType() {
        return material;
    }

    public byte getData() {
        return (byte) data;
    }

    public void setBlockAt(Location loc) {
        Block b = loc.getBlock();
        b.setType(getType());
        b.setData(getData());
    }
}
