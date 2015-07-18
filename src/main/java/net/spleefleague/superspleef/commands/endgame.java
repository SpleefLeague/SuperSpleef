/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.commands;

import net.spleefleague.core.chat.Theme;
import net.spleefleague.core.command.BasicCommand;
import net.spleefleague.core.player.PlayerState;
import net.spleefleague.core.player.SLPlayer;
import net.spleefleague.core.plugin.CorePlugin;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Battle;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class endgame extends BasicCommand {

    public endgame(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if(slp.getState() == PlayerState.INGAME) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
            sp.setRequestingEndgame(true);
            Battle battle = sp.getCurrentBattle();
            boolean shouldEnd = true;
            for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                if(!spleefplayer.isRequestingEndgame()) {
                    shouldEnd = false;
                    break;
                }
            }
            if(shouldEnd) {
                battle.cancel(false);
                for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    spleefplayer.setRequestingEndgame(false);
                }
            }
            else {
                for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    if(!spleefplayer.isRequestingEndgame()) {
                        spleefplayer.getPlayer().sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to end this game. To agree enter " + ChatColor.YELLOW + "/endgame.");
                    }
                }
                slp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested this game to be cancelled.");
            }
        }
        else {
            error(p, "You are not ingame!");
        }
    }
}
