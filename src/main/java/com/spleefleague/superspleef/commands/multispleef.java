/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.multi.MultiSpleefArena;

/**
 *
 * @author jonas
 */
public class multispleef extends spleefCommand<MultiSpleefArena> {
    
    public multispleef(CorePlugin plugin, String name, String usage) {
        super(plugin, SpleefMode.MULTI.getChatPrefix(), name, usage, arena -> MultiSpleefArena.byName(arena), SuperSpleef.getInstance().getMultiSpleefBattleManager());
    }
}
