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

/**
 *
 * @author jonas
 */
public class PowerSpleefBattle extends NormalSpleefBattle {

    private HashMap<SpleefPlayer, Power> powers;
    
    public PowerSpleefBattle(Arena arena, List<SpleefPlayer> players) {
        super(arena, players);
        for(SpleefPlayer sp : players) {
            powers.put(sp, sp.getPowerType().createPower(sp));
        }
    }

    public void handlePowerRequest(SpleefPlayer sp) {
        Power power = powers.get(sp);
        if(!power.isOnCooldown()) {
            power.tryRun();
        }
    }
}
