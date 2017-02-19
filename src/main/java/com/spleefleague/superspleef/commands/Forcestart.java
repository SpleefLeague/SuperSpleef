/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.utils.Debugger;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.TeamSpleefArena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class Forcestart implements Debugger {
    
    private TeamSpleefArena arena = TeamSpleefArena.byName("Spire");
    
    @Override
    public void debug() {
        Player player = Bukkit.getPlayer("Joba");
        player.sendRawMessage("/tell Knus test");
        List<SpleefPlayer> players = new ArrayList<>();
        players.add(SuperSpleef.getInstance().getPlayerManager().get("ShadowFlames"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("AdolfCritler_"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Harambe_God"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("ChillingNKilling"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Tpindell"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Triffuny"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Blazeoden"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("sheriffCy"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("axitor"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("E93"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Joba"));
        players.add(SuperSpleef.getInstance().getPlayerManager().get("Knus"));
        arena.startBattle(players, BattleStartEvent.StartReason.FORCE);
    }
}
