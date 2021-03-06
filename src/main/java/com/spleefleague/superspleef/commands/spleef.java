/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.classic.ClassicSpleefArena;

/**
 *
 * @author jonas
 */
public class spleef extends spleefCommand<ClassicSpleefArena> {
    
    public spleef(CorePlugin plugin, String name, String usage) {
        super(plugin, SpleefMode.CLASSIC.getChatPrefix(), name, usage, arena -> ClassicSpleefArena.byName(arena), SuperSpleef.getInstance().getClassicSpleefBattleManager());
    }
}
