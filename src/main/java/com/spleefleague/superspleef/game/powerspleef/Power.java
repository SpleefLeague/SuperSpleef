package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Bukkit;

/**
 *
 * @author balsfull
 */
public abstract class Power {

    private int cooldown;
    private final int maxCooldown;
    private final PowerType type;
    private final SpleefPlayer player;
    
    public Power(PowerType type, SpleefPlayer player, int maxCooldown) {
        this.type = type;
        this.maxCooldown = maxCooldown;
        this.player = player;
    }

    public PowerType getType() {
        return type;
    }

    public SpleefPlayer getPlayer() {
        return player;
    }
    
    public void tryRun() {
        boolean hasRun = execute();
        if(hasRun) {
            this.cooldown = maxCooldown;
            onCooldown.add(this);
        }
    }
    
    public abstract boolean execute();
    public abstract void cancel();
    public abstract void destroy();
    
    public boolean isOnCooldown() {
        return cooldown > 0;
    }
    
    public int getCooldown() {
        return cooldown;
    }
    
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
    
    private static Set<Power> onCooldown;
    
    protected static Function<SpleefPlayer, ? extends Power> emptyPower() {
        return (sp) -> new Power(PowerType.NO_POWER, sp, 0) {
            @Override
            public boolean execute() {
                return false;
            }

            @Override
            public void cancel() {}

            @Override
            public void destroy() {}
        };
    }
    
    public static void init() {
        Bukkit.getScheduler().runTaskTimer(SuperSpleef.getInstance(), () -> {
            onCooldown.forEach(p -> p.cooldown--);
            onCooldown.removeIf(p -> p.cooldown <= 0);
        }, 0, 1);
    }
    
    static {
        onCooldown = new HashSet<>();
    }
}
