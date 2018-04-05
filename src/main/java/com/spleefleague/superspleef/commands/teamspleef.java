/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.team.TeamSpleefArena;

/**
 *
 * @author jonas
 */
public class teamspleef extends spleefCommand<TeamSpleefArena> {
    
    public teamspleef(CorePlugin plugin, String name, String usage) {
        super(plugin, name, usage, arena -> TeamSpleefArena.byName(arena), SuperSpleef.getInstance().getTeamSpleefBattleManager());
    }
}
