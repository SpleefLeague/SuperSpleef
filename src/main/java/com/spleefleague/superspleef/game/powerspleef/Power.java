package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.function.Function;

/**
 *
 * @author balsfull
 */
public abstract class Power {
    
    private final SpleefPlayer player;
    private final PowerType powerType;
    
    public Power(SpleefPlayer player, PowerType powerType) {
        this.player = player;
        this.powerType = powerType;
    }

    public PowerType getPowerType() {
        return powerType;
    }
    
    public SpleefPlayer getPlayer() {
        return player;
    }
    
    protected SpleefBattle getBattle() {
        return getPlayer().getCurrentBattle();
    }
    
    public void init() {
        
    }
    
    public void cleanup() {
        
    }
    
    public void initRound() {
        
    }
    
    public void cleanupRound() {
        
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
