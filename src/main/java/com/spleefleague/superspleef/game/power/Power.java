package com.spleefleague.superspleef.game.power;

import com.spleefleague.core.utils.PlayerUtil;
import com.spleefleague.core.utils.scheduler.PredicateScheduler;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author balsfull
 */
public abstract class Power {
    
    private final SpleefPlayer player;
    private final PowerType powerType;
    private BukkitTask durationTask;
    private final Map<ItemStack, NBTTagList> oldCanDestroy;
    
    public Power(SpleefPlayer player, PowerType powerType) {
        this.player = player;
        this.powerType = powerType;
        this.oldCanDestroy = new HashMap<>();
    }

    public PowerType getPowerType() {
        return powerType;
    }
    
    public SpleefPlayer getPlayer() {
        return player;
    }
    
    protected PowerSpleefBattle getBattle() {
        return (PowerSpleefBattle)getPlayer().getCurrentBattle();
    }
    
    public void init() {
        
    }
    
    public void cleanup() {
        
    }
    
    public void initRound() {
        
    }
    
    public void cleanupRound() {
        
    }
    
    protected void cancelDuration() {
        if(durationTask != null) {
            durationTask.cancel();
            durationTask = null;
        }
    }
    
    protected void showDuration(int ticks) {
        showDuration(null, ticks);
    }
    
    protected void showDuration(String name, int ticks) {
        cancelDuration();
        durationTask = PredicateScheduler.runTaskTimer(SuperSpleef.getInstance(), new Runnable() {
            
            private int timeLeft = ticks;
            private final char square = '■';
            
            @Override
            public void run() {
                double percentageFull = timeLeft / (double)ticks;
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GRAY).append('[').append(ChatColor.GREEN);
                boolean red = false;
                for (int i = 11; i >= 0; i--) {
                    double p = i/11.0;
                    if(!red && percentageFull > p) {
                        sb.append(ChatColor.RED);
                        red = true;
                    }
                    sb.append(square);
                }
                sb.append(ChatColor.GRAY).append("] ");
                if(name == null) {
                    int secs = timeLeft / 20;
                    int hsecs = (1 + (timeLeft % 20)) / 2;
                    sb.append(ChatColor.GOLD).append(secs).append(".").append(hsecs).append(" seconds.");
                }
                else {
                    sb.append(ChatColor.GOLD).append(name);
                }
                PlayerUtil.actionbar(player, new ChatMessage(sb.toString()), 20, false);
                timeLeft = Math.max(0, timeLeft - 2);
            }
        }, 0, 2, 2 + ticks / 2);
    }
    
    protected void setItemsEnabled(SpleefPlayer sp, boolean enabled, int duration) {
        Inventory inventory = sp.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if(item == null || item.getType() == Material.AIR) continue;
            net.minecraft.server.v1_15_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
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
    
    public abstract boolean tryExecute();
    
    public static Function<SpleefPlayer, Power> emptyPower() {
        return s -> new Power(s, PowerType.EMPTY_POWER) {
            @Override
            public boolean tryExecute() {
                return true;
            }
        };
    }
    
    public static void startSchedulers() {
        ChargePower.startSchedulers();
        CooldownPower.startSchedulers();
    }
}
