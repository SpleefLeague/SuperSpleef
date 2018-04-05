/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.cosmetics;

import com.mongodb.client.MongoCursor;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author jonas
 */
public class Shovel extends DBEntity implements DBLoadable, DBSaveable {
    
    @DBLoad(fieldName = "name")
    @DBSave(fieldName = "name")
    private String name;
    @DBLoad(fieldName = "text")
    @DBSave(fieldName = "text")
    private String[] text;
    @DBLoad(fieldName = "damage")
    @DBSave(fieldName = "damage")
    private short damage;
    @DBLoad(fieldName = "material")
    @DBSave(fieldName = "material")
    private Material type;
    @DBLoad(fieldName = "default")
    @DBSave(fieldName = "default")
    private boolean isDefault = false;
    
    public Shovel() {
        
    }
    
    public Shovel(Material type, short damage, String name, String[] description, boolean isDefault) {
        this.name = name;
        this.text = description;
        this.damage = damage;
        this.type = type;
        this.isDefault = isDefault;
    }
    
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(type, 1, damage);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(text));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
        is.setItemMeta(im);
        return is;
    }

    public boolean isIsDefault() {
        return isDefault;
    }
    
    public String getName() {
        return name;
    }

    public String[] getDescription() {
        return text;
    }

    public short getDamage() {
        return damage;
    }
    
    public Material getType() {
        return type;
    }
    
    private static final Map<Short, Shovel> SHOVELS = new HashMap<>();
    public static final Shovel DEFAULT_SHOVEL;
    
    static {
        DEFAULT_SHOVEL = new Shovel(Material.DIAMOND_SPADE, (byte)0, "Default Shovel", new String[0], true);
    }
    
    public static Shovel byDamageValue(short damage) {
        return SHOVELS.get(damage);
    }
    
    public static Collection<Shovel> getAll() {
        return SHOVELS.values();
    }
    
    public static boolean addShovel(Shovel shovel) {
        return shovel != SHOVELS.putIfAbsent(shovel.getDamage(), shovel);
    }
    
    public static void init() {
        SHOVELS.clear();
        Document query = new Document("disabled", new Document("$ne", true));
        MongoCursor<Document> dbc = SuperSpleef.getInstance().getPluginDB()
                .getCollection("Shovels")
                .find(query)
                .iterator();
        while (dbc.hasNext()) {
            Document d = dbc.next();
            try {
                Shovel shovel = EntityBuilder.load(d, Shovel.class);
                if (shovel == null) continue;
                SHOVELS.put(shovel.getDamage(), shovel);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading shovel: " + d.get("damage"));
            }
        }
        SuperSpleef.getInstance().log("Loaded " + SHOVELS.size() + " shovels!");
    }
}
