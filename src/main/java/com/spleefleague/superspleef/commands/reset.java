/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import static com.spleefleague.annotations.CommandSource.PLAYER;
import com.spleefleague.annotations.Endpoint;
import com.spleefleague.annotations.LiteralArg;
import com.spleefleague.annotations.PlayerArg;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.commands.command.BasicCommand;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class reset extends BasicCommand {

    public reset(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), new resetDispatcher(), name, usage);
    }

    @Endpoint(target = {PLAYER})
    public void resetOwnForce(SLPlayer slp, @LiteralArg(value = "force") String l) {
        if (!slp.getRank().hasPermission(Rank.MODERATOR)) {
            sendUsage(slp);
            return;
        }
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(slp);
        if (!sp.isIngame()) {
            error(sp, "You are currently not ingame.");
            return;
        }
        SpleefBattle<?> battle = sp.getCurrentBattle();
        //battle.resetField();
        battle.startRound();

        for (SpleefPlayer spleefplayer : battle.getAlivePlayers()) {
            spleefplayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false)
                    + "Your battle has been reset by a moderator.");
            //spleefplayer.teleport(battle.getData(spleefplayer).getSpawn());
        }
        success(slp, "The battle has been reset.");

    }

    @Endpoint(target = {PLAYER})
    public void resetOtherForce(SLPlayer slp, @PlayerArg Player target) {
        if (!slp.getRank().hasPermission(Rank.MODERATOR)) {
            sendUsage(slp);
            return;
        }
        SpleefBattle<?> battle = SuperSpleef.getInstance().getPlayerManager().get(target).getCurrentBattle();
        //battle.resetField();
        battle.startRound();
        
        for (SpleefPlayer spleefplayer : battle.getAlivePlayers()) {
            spleefplayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false)
                    + "Your battle has been reset by a moderator.");
            spleefplayer.teleport(battle.getData(spleefplayer).getSpawn());
        }
        success(slp, "The battle has been reset.");
    }

    @Endpoint(target = {PLAYER})
    public void resetRequest(Player p) {
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        if (!sp.isIngame()) {
            error(sp, "You are currently not ingame.");
            return;
        }
        sp.setRequestingReset(true);
        SpleefBattle<?> battle = sp.getCurrentBattle();
        battle.onScoreboardUpdate();
        int requesting = 0, total = 0;
        for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
            if (spleefplayer.isRequestingReset()) {
                requesting++;
            }
            total++;
        }
        if ((double) requesting / (double) total > 0.65) {
            //battle.resetField();
            battle.startRound();
            for (SpleefPlayer spleefplayer : battle.getAlivePlayers()) {
                spleefplayer.setRequestingReset(false);
                spleefplayer.teleport(battle.getData(spleefplayer).getSpawn());
            }
            battle.onScoreboardUpdate();
        } else {
            for (SpleefPlayer spleefplayer : battle.getActivePlayers()) {
                if (!spleefplayer.isRequestingReset()) {
                    spleefplayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to reset the field. To agree enter " + ChatColor.YELLOW + "/reset.");
                }
            }
            p.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested a reset of the field.");
        }
    }
}
