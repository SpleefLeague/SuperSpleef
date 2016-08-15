package com.spleefleague.superspleef.commands;

import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.utils.ModifiableFinal;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author RinesThaix
 */
public class playto extends BasicCommand {

    public playto(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if (slp.getState() != PlayerState.INGAME) {
            error(p, "You are currently not in a game.");
            return;
        }
        if (args.length < 1) {
            sendUsage(p);
            return;
        }
        final int to;
        try {
            to = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendUsage(p);
            return;
        }
        if (to <= 0 || to > 100) {
            error(p, "You can only play between 1-100 rounds!");
            return;
        }
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        sp.setPlayToRequest(to);
        SpleefBattle battle = sp.getCurrentBattle();
        Set<SpleefPlayer> requesting = new HashSet<>();
        ModifiableFinal<Integer> total = new ModifiableFinal<>(0);
        battle.getActivePlayers().forEach((SpleefPlayer spleefPlayer) -> {
            total.setValue(total.getValue() + 1);
            int request = spleefPlayer.getPlayToRequest();
            if (request != -1 && request == to) {
                requesting.add(spleefPlayer);
            }
        });
        if (requesting.size() == total.getValue()) {
            battle.getPlayers().forEach((SpleefPlayer spleefPlayer) -> {
                spleefPlayer.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) +
                        "The game has been set to first to " + to + ".");
            });
            battle.changePointsCup(to);
            battle.onScoreboardUpdate();
        } else {
            battle.getActivePlayers().stream().filter(player -> !requesting.contains(player)).forEach(player -> {
                player.sendMessage(
                        SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) +
                                "Your opponent wants to change the game to first to " + to + ". To agree enter " +
                                ChatColor.YELLOW + "/playto " + to + ".");
            });
            slp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) +
                            "You requested to change the game to first to " + to + ".");
        }
    }

}
