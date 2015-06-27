/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.commands;

import net.spleefleague.core.utils.Debugger;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Arena;

/**
 *
 * @author Jonas
 */
public class Debug implements Debugger {

    @Override
    public void debug() {
        for(Arena arena : Arena.getAll()) {
            System.out.println(SuperSpleef.getInstance().getBattleManager().getGameQueue().getQueueLength(arena));
        }
    }
}
