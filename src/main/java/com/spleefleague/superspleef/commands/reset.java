/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;


/**
 *
 * @author Jonas
 */
public class reset extends BasicCommand {

    public reset(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if(slp.getState() == PlayerState.INGAME) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
            sp.setRequestingReset(true);
            SpleefBattle battle = sp.getCurrentBattle();
            int requesting = 0, total = 0;
            for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                if(spleefplayer.isRequestingReset()) {
                    requesting++;
                }
                total++;
            }
            if((double)requesting / (double)total > 0.65) {
                battle.resetField();
                for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    spleefplayer.setRequestingReset(false);
                    spleefplayer.teleport(battle.getData(spleefplayer).getSpawn());
                }
            }
            else {
                for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                    if(!spleefplayer.isRequestingReset()) {
                        spleefplayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to reset the field. To agree enter " + ChatColor.YELLOW + "/reset.");
                    }
                }
                slp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested a reset of the field.");
            }
        }
        else if(slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
            if(args.length == 1) {
                Player player = Bukkit.getPlayer(args[0]);
                if(player != null) {
                    SpleefPlayer target = SuperSpleef.getInstance().getPlayerManager().get(player);
                    SpleefBattle battle = target.getCurrentBattle();
                    if(battle != null) {
                        battle.resetField();
                        for(SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                            spleefplayer.sendMessage(SuperSpleef.getInstance().getPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your battle has been reset by a moderator.");
                            spleefplayer.teleport(battle.getData(spleefplayer).getSpawn());
                        }
                        success(p, "The battle has been reset.");
                    }
                    else {
                        error(p, player.getName() + " is currently not ingame.");
                    }
                }
                else {
                    error(p, args[0] + " is currently not online!");
                }
            }
            else {
                sendUsage(p);
            }
        }
        else {
            error(p, "You are currently not ingame.");
        }
    }
}
