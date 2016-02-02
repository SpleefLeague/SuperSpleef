/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.superspleef.commands;

import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/*
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
            success(p, "You successfully joined the SWC qualifiers! You need to win games to qualify for the groupstage.");
            sp.setJoinedSWC(true);
        }
        else {
            error(p, "Please contact the staff if you do not want to participate in the SWC.");
        }
    }
}
