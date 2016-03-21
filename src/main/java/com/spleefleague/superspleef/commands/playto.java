package com.spleefleague.superspleef.commands;

import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Battle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 *
 * @author RinesThaix
 */
public class playto extends BasicCommand {
    
    private final static Map<Player, Integer> requested = new HashMap();
    
    public static void invalidate(Player p) {
        requested.remove(p);
    }

    public playto(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if(slp.getState() != PlayerState.INGAME) {
            error(p, "You are currently not ingame.");
            return;
        }
        if(args.length != 1) {
            sendUsage(p);
            return;
        }
        int to = 0;
        try {
            to = Integer.parseInt(args[0]);
        }catch(NumberFormatException ex) {
            sendUsage(p);
            return;
        }
        if(to <= 0 || to > 1000) {
            error(p, "&cThis number is not valid.");
            return;
        }
        requested.put(p, to);
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        Battle battle = sp.getCurrentBattle();
        Set<SpleefPlayer> requesting = new HashSet();
        int total = 0;
        for(SpleefPlayer player : battle.getActivePlayers()) {
            ++total;
            Integer i = requested.get(player);
            if(i != null && i == to)
                requesting.add(player);
        }
        if((double) requesting.size() / total > 0.65d) {
            for(SpleefPlayer player : battle.getPlayers())
                player.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "The game has been set to first to " + to + ".");
            battle.changePointsCup(to);
        }else {
            for(SpleefPlayer player : battle.getActivePlayers()) {
                if(!requesting.contains(player)) {
                    player.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "Your opponent wants to change the game to first to " + to + ". To agree enter " + ChatColor.YELLOW + "/playto " + to + ".");
                }
            }
            slp.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.WARNING.buildTheme(false) + "You requested to change the game to first to " + to + ".");
        }
    }

}
