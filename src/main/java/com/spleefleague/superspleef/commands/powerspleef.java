/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.power.PowerSpleefArena;
import com.spleefleague.superspleef.game.power.PowerType;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author jonas
 */
public class powerspleef extends spleefCommand<PowerSpleefArena> {
    
    public powerspleef(CorePlugin plugin, String name, String usage) {
        super(plugin, SpleefMode.POWER.getChatPrefix(), name, usage, arena -> PowerSpleefArena.byName(arena), SuperSpleef.getInstance().getPowerSpleefBattleManager());
    }
    
    @Override
    public void challenge(SLPlayer sender, String l, String arenaName, Player[] players) {
        if(checkPowerSelected(sender)) {
            for (Player player : players) {
                if(!hasPowerSelected(player)) {
                    error(sender, player.getName() + " has not selected a power yet.");
                    return;
                }
            }
            super.challenge(sender, l, arenaName, players);
        }
    }
    
    @Override
    public void queueGlobally(Player sender) {
        if(checkPowerSelected(sender)) { 
            super.queueGlobally(sender);
        }
    }

    @Override
    public void queueArena(Player sender, String arenaName) {
        if(checkPowerSelected(sender)) { 
            super.queueArena(sender, arenaName);
        }
    }
    
    private boolean checkPowerSelected(Player player) {
        if(!hasPowerSelected(player)) {
            error(player, "Please select a power in the SL Menu!");
            return false;
        }
        return true;
    }
    
    private boolean hasPowerSelected(Player player) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(player);
        if(sp.getPowerType() == PowerType.EMPTY_POWER) {
            return false;
        }
        return true;
    }
}
