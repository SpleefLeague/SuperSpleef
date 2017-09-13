/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.NormalSpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jonas
 */
public class PowerSpleefBattle extends NormalSpleefBattle {

    private Map<SpleefPlayer, Power> powers;
    
    public PowerSpleefBattle(Arena arena, List<SpleefPlayer> players) {
        super(arena, players);
        this.powers = new HashMap<>();
        for(SpleefPlayer sp : players) {
            powers.put(sp, sp.getPowerType().createPower(sp));
        }
    }

    public void handlePowerRequest(SpleefPlayer sp) {
        Power power = powers.get(sp);
        System.out.println("Checking cooldown for " + power.getType().getDisplayName());
        if(!power.isOnCooldown()) {
            System.out.println("It's available");
            power.tryRun();
        }
    }
    
    @Override
    public void resetPlayer(SpleefPlayer sp) {
        Power power = powers.get(sp);
        if(power != null) {
            power.cancel();
        }
        super.resetPlayer(sp);
    }
}
