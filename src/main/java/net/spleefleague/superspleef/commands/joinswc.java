/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.spleefleague.superspleef.commands;

import net.spleefleague.core.command.BasicCommand;
import net.spleefleague.core.player.SLPlayer;
import net.spleefleague.core.plugin.CorePlugin;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author Manuel
 */
public class joinswc extends BasicCommand{

    public joinswc(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        if(!sp.joinedSWC()) {
            success(p, "You successfully joined the SWC! You need to win games to qualify for the groupstage.");
            sp.setJoinedSWC(true);
        }
        else {
            error(p, "You can't join the SWC twice.");
        }
    }
    
}
