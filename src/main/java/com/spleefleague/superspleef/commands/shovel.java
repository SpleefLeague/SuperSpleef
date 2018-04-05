/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.mongodb.client.MongoCollection;
import static com.spleefleague.annotations.CommandSource.COMMAND_BLOCK;
import static com.spleefleague.annotations.CommandSource.CONSOLE;
import static com.spleefleague.annotations.CommandSource.PLAYER;
import com.spleefleague.annotations.Endpoint;
import com.spleefleague.annotations.IntArg;
import com.spleefleague.annotations.LiteralArg;
import com.spleefleague.annotations.SLPlayerArg;
import com.spleefleague.commands.command.BasicCommand;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.cosmetics.Shovel;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author jonas
 */
public class shovel extends BasicCommand {

    private final List<Material> allowedTypes = Arrays.asList(new Material[]{
        Material.DIAMOND_SPADE, 
        Material.GOLD_SPADE, 
        Material.IRON_SPADE, 
        Material.STONE_SPADE, 
        Material.WOOD_SPADE
    });
    
    public shovel(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), new shovelDispatcher(), name, usage, Rank.ADMIN);
    }
    
    @Endpoint
    public void reloadShovels(CommandSender sender, @LiteralArg(value = "reload") String l) {
        Shovel.init();
        for(SpleefPlayer sp : SuperSpleef.getInstance().getPlayerManager().getAll()) {
            Set<Shovel> updated = sp.getAvailableShovels()
                    .stream()
                    .map(s -> Shovel.byDamageValue(s.getDamage()))
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());
            boolean keepActive = 
                    Shovel.byDamageValue(sp.getActiveShovel().getDamage()).isIsDefault() || 
                    sp.getAvailableShovels().contains(sp.getActiveShovel());
            sp.getAvailableShovels().clear();
            if(!keepActive) {
                sp.setActiveShovel(Shovel.DEFAULT_SHOVEL);
            }
            else {
                sp.setActiveShovel(Shovel.byDamageValue(sp.getActiveShovel().getDamage()));
            }
            sp.getAvailableShovels().addAll(updated);
        }
        success(sender, "Reloaded shovels.");
    }
    
    @Endpoint(target = {PLAYER, CONSOLE, COMMAND_BLOCK})
    public void addShovel(CommandSender sender, @LiteralArg(value = "add") String l, @SLPlayerArg SLPlayer target, @IntArg(min = 0, max = Short.MAX_VALUE) int shovelId) {
        Shovel shovel = Shovel.byDamageValue((short)shovelId);
        if(shovel == null) {
            error(sender, "This shovel does not exist.");
            return;
        }
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(target);
        sp.getAvailableShovels().add(shovel);
        success(sender, "Shovel has been added");
    }
    
    @Endpoint(target = {PLAYER, CONSOLE, COMMAND_BLOCK})
    public void removeShovel(CommandSender sender, @LiteralArg(value = "remove") String l, @SLPlayerArg SLPlayer target, @IntArg(min = 0, max = Short.MAX_VALUE) int shovelId) {
        Shovel shovel = Shovel.byDamageValue((short)shovelId);
        if(shovel == null) {
            error(sender, "This shovel does not exist.");
            return;
        }
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(target);
        sp.getAvailableShovels().remove(shovel);
        success(sender, "Shovel has been added");
    }
    
    @Endpoint(target = {PLAYER})
    public void createShovelNotDefault(SLPlayer slp, @LiteralArg(value = "create") String l1) {
        createShovel(slp, false);
    }
    
    @Endpoint(target = {PLAYER})
    public void createShovelNotDefault(SLPlayer slp, @LiteralArg(value = "create") String l1, @LiteralArg(value = "default") String l2) {
        createShovel(slp, true);
    }
    
    private void createShovel(SLPlayer slp, boolean isDefault) {
        ItemStack is = slp.getInventory().getItemInMainHand();
        if(is != null && allowedTypes.contains(is.getType())) {
            ItemMeta im = is.getItemMeta();
            String[] lore;
            if(im.getLore() == null) {
                lore = new String[0];
            }
            else {
                lore = im.getLore().toArray(new String[0]);
            }
            Shovel shovel = new Shovel(is.getType(), is.getDurability(), im.getDisplayName(), lore, isDefault);
            boolean success = Shovel.addShovel(shovel);
            if(!success) {
                error(slp, "A shovel with this durability already exists. Name: " + Shovel.byDamageValue(shovel.getDamage()).getName());
            }
            Bukkit.getScheduler().runTaskAsynchronously(SuperSpleef.getInstance(), () -> {
                try {
                    MongoCollection<Document> col = SuperSpleef.getInstance().getPluginDB().getCollection("Shovels");
                    Document doc = EntityBuilder.serialize(shovel).get("$set", Document.class);
                    col.insertOne(doc);
                    success(slp, "Shovel created.");
                } catch(Exception e) {
                    int id = (int)(Math.random() * 1000);
                    error(slp, "DB insertion error! ID: " + id);
                    System.err.println("DB insertion error: " + id);
                    e.printStackTrace();
                }
            });
        }
        else {
            error(slp, "The item in your hand is not a valid shovel.");
        }
    }
}
