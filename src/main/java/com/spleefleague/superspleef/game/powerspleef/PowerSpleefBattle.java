/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.powerspleef;

import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.RemoveReason;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jonas
 */
public class PowerSpleefBattle extends SpleefBattle<PowerSpleefArena> {
    
    private final Map<SpleefPlayer, Power> powers;
    
    public PowerSpleefBattle(PowerSpleefArena arena, List<SpleefPlayer> players) {
        super(arena, players);
        powers = players.stream().collect(Collectors.toMap(Function.identity(), sp -> sp.getPowerType().createPower(sp)));
    }
    
    public void requestPowerUse(SpleefPlayer sp) {
        this.isInCountdown();
    }
    
    @Override
    public void start(BattleStartEvent.StartReason reason) {
        super.start(reason);
        SuperSpleef.getInstance().getPowerSpleefBattleManager().add(this);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        SuperSpleef.getInstance().getPowerSpleefBattleManager().remove(this);
    }
    
    @Override
    public void resetPlayer(SpleefPlayer sp) {
        super.resetPlayer(sp);
        powers.get(sp).cleanupRound();
        powers.get(sp).cleanup();
    }
    
    @Override
    public void removePlayer(SpleefPlayer sp, RemoveReason reason) {
        super.removePlayer(sp, reason);
        powers.remove(sp);
    }
    
    @Override
    public void onRoundStart() {
        powers.values().forEach(Power::initRound);
    }
    
    @Override
    protected void applyRatingChange(SpleefPlayer winner) {
        System.out.println("Not implemented yet!");
    }
}
