package com.spleefleague.superspleef.commands;

import static com.spleefleague.annotations.CommandSource.PLAYER;
import com.spleefleague.annotations.Endpoint;
import com.spleefleague.annotations.IntArg;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.commands.command.BasicCommand;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.utils.ModifiableFinal;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;

import java.util.*;

/**
 * @author RinesThaix
 */
public class playto extends BasicCommand {

    public playto(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), new playtoDispatcher(), name, usage);
    }

    @Endpoint(target = {PLAYER})
    public void playto(SLPlayer slp, @IntArg(min = 1, max = 100) int to) {
        if (slp.getState() != PlayerState.INGAME) {
            error(slp, "You are currently not in a game.");
            return;
        }
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(slp);
        sp.setPlayToRequest(to);
        SpleefBattle<?> battle = sp.getCurrentBattle();
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