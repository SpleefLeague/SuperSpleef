package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Bukkit;

/**
 *
 * @author balsfull
 */
public abstract class Power {

    private int cooldown;
    private final int maxCooldown;
    private final PowerType type;
    
    public Power(PowerType type, int maxCooldown) {
        this.type = type;
        this.maxCooldown = maxCooldown;
    }

    public PowerType getType() {
        return type;
    }
    
    public void tryRun(SpleefPlayer player) {
        boolean hasRun = execute(player);
        if(hasRun) {
            this.cooldown = maxCooldown;
            onCooldown.add(this);
        }
    }
    
    public abstract boolean execute(SpleefPlayer player);
    public abstract void cancel();
    
    public boolean isOnCooldown() {
        return cooldown > 0;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    private static Map<String, Supplier<? extends Power>> powers;
    private static Set<Power> onCooldown;
    
    public Power getByName(String name) {
        Supplier<? extends Power> power = powers.get(name);
        if(power == null) {
            return null;
        }
        else {
            return power.get();
        }
    }
    
    public static void addPower(String name, Supplier<? extends Power> supplier) {
        powers.put(name, supplier);
    }
    
    static {
        powers = new HashMap<>();
        onCooldown = new HashSet<>();
        Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            onCooldown.forEach(p -> p.cooldown--);
            onCooldown.removeIf(p -> p.cooldown <= 0);
        }, 0, 1);
    }
}
