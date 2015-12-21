/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.scoreboards;

import com.spleefleague.core.io.DBEntity;
import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.DBLoadable;
import com.spleefleague.core.io.DBSaveable;
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