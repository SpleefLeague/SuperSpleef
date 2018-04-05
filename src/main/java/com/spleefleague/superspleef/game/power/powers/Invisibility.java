/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power.powers;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.power.CooldownPower;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author jonas
 */
public class Invisibility extends CooldownPower {

    private final int duration;
    private final Map<ItemStack, NBTTagList> oldCanDestroy;
    private BukkitTask activeTask;
    
    private Invisibility(SpleefPlayer sp, int cooldown, int duration) {
        super(sp, PowerType.INVISIBILITY, cooldown);
        this.duration = duration;
        this.oldCanDestroy = new HashMap<>();
    }

    @Override
    public void execute() {
        if(activeTask != null) {
            activeTask.cancel();
        }
        SpleefPlayer player = getPlayer();
        Stream.of(
                getBattle().getActivePlayers().stream(), 
                getBattle().getSpectators().stream())
                .flatMap(Function.identity())
                .filter(sp -> sp != player)
                .forEach(sp -> {
                    sp.hidePlayer(player.getPlayer());
                });
        setItemsEnabled(player, false);
        activeTask = Bukkit.getScheduler().runTaskLater(SuperSpleef.getInstance(), () -> {
            cleanupRound();
        }, duration);
    }

    @Override
    public void cleanupRound() {
        if(activeTask != null) {
            activeTask.cancel();
            activeTask = null;
            getBattle().setVisibility(getPlayer());
            setItemsEnabled(getPlayer(), true);
        }
        oldCanDestroy.clear();
    }
    
    private void setItemsEnabled(SpleefPlayer sp, boolean enabled) {
        Inventory inventory = sp.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR) continue;
            net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = stack.hasTag() ? stack.getTag() : new NBTTagCompound();
            if(enabled) {
                if(!oldCanDestroy.containsKey(item)) continue;
                tag.set("CanDestroy", oldCanDestroy.get(item));
                oldCanDestroy.remove(item);
                item = CraftItemStack.asBukkitCopy(stack);
                sp.setCooldown(item.getType(), 0);
            }
            else {
                if(!tag.hasKey("CanDestroy")) continue;
                NBTTagList current = (NBTTagList)tag.get("CanDestroy");
                tag.remove("CanDestroy");
                item = CraftItemStack.asBukkitCopy(stack);
                sp.setCooldown(item.getType(), duration);
                oldCanDestroy.put(item, current);
            }
            inventory.setItem(i, item);
        }
    }
    
    public static Function<SpleefPlayer, Invisibility> getSupplier() {
        return sp -> new Invisibility(sp, 400, 70);
    }
}
