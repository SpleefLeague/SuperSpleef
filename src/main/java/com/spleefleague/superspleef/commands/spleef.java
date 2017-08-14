/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.BattleManager;
import com.spleefleague.core.queue.Challenge;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.TeamSpleefArena;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jonas
 */
public class spleef extends BasicCommand {

    public spleef(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if (SuperSpleef.getInstance().queuesOpen()) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
            BattleManager bm = SuperSpleef.getInstance().getNormalSpleefBattleManager();
            if (!GamePlugin.isIngameGlobal(p)) {
                if (args.length == 0) {
                    GamePlugin.dequeueGlobal(p);
                    bm.queue(sp);
                    success(p, "You have been added to the queue.");
                } else if (args.length == 1) {
                    Arena arena = Arena.byName(args[0]);
                    if (arena != null && arena.getSpleefMode() == SpleefMode.NORMAL) {
                        if (!arena.isPaused()) {
                            if (arena.isAvailable(sp)) {
                                bm.queue(sp, arena);
                                success(p, "You have been added to the queue for: " + ChatColor.GREEN + arena.getName());
                            } else {
                                error(p, "You have not visited this arena yet!");
                            }
                        } else {
                            error(p, "This arena is currently paused.");
                        }
                    } else {
                        error(p, "This arena does not exist.");
                    }
                } else if (args.length >= 2 && args[0].equalsIgnoreCase("match")) {
                    if (slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
                        Arena arena = Arena.byName(args[1]);
                        if (arena != null && arena.getSpleefMode() == SpleefMode.NORMAL) {
                            if (!arena.isOccupied()) {
                                if ((args.length - 2) == arena.getSize()) {
                                    ArrayList<SpleefPlayer> players = new ArrayList<>();
                                    for (int i = 0; i < args.length - 2; i++) {
                                        Player pl = Bukkit.getPlayer(args[i + 2]);
                                        if (pl != null) {
                                            players.add(SuperSpleef.getInstance().getPlayerManager().get(pl));
                                        } else {
                                            error(p, "The player " + args[i + 2] + " is currently not online.");
                                            return;
                                        }
                                    }
                                    arena.startBattle(players, StartReason.FORCE);
                                    success(p, "You started a battle on the arena " + arena.getName());
                                } else {
                                    error(p, "You need to list " + (args.length - 2) + " players for this arena.");
                                }
                            } else {
                                error(p, "This arena is currently occupied.");
                            }
                        } else {
                            error(p, "This arena does not exist.");
                        }
                    } else {
                        sendUsage(p);
                    }
                } else if (args.length == 2) {
                    if (slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
                        Arena arena = Arena.byName(args[1]);
                        if (arena != null && arena.getSpleefMode() == SpleefMode.NORMAL) {
                            if (args[0].equalsIgnoreCase("pause")) {
                                arena.setPaused(true);
                                success(p, "You have paused the arena " + arena.getName());
                            } else if (args[0].equalsIgnoreCase("unpause")) {
                                arena.setPaused(false);
                                success(p, "You have unpaused the arena " + arena.getName());
                            } else {
                                sendUsage(p);
                            }
                            GameSign.updateGameSigns(arena);
                            EntityBuilder.save(arena, SuperSpleef.getInstance().getPluginDB().getCollection("Arenas"));
                        } else {
                            error(p, "This arena does not exist.");
                        }
                    } else {
                        sendUsage(p);
                    }
                } else if (args.length == 3 && (args[0].equalsIgnoreCase("challenge") || args[0].equalsIgnoreCase("c"))) {
                    Arena arena = Arena.byName(args[1]);
                    if (arena != null) {
                        if (!arena.isAvailable(sp)) {
                            error(p, "You have not discovered this arena");
                            return;
                        }
                        if (args.length - 1 == arena.getSize()) {
                            SLPlayer[] players = new SLPlayer[arena.getSize()-1];
                            for (int i = 2; i < args.length; i++) {
                                Player t = Bukkit.getPlayer(args[i]);
                                if (t != null) {
                                    if (t == p) {
                                        error(p, "You may not challenge yourself.");
                                        return;
                                    }
                                    SLPlayer splayer = SpleefLeague.getInstance().getPlayerManager().get(t.getUniqueId());
                                    if (splayer.getState() == PlayerState.INGAME) {
                                        error(p, splayer.getName() + " is currently ingame!");
                                        return;
                                    }
                                    SpleefPlayer spt = SuperSpleef.getInstance().getPlayerManager().get(t.getUniqueId());
                                    if (!arena.isAvailable(spt)) {
                                        error(p, spt.getName() + " has not visited this arena yet!");
                                        return;
                                    }
                                    players[i-2] = splayer;
                                } else {
                                    error(p, "The player " + args[i] + " is not online.");
                                    return;
                                }
                            }
                            Challenge challenge = new Challenge(slp, players) {
                                @Override
                                public void start(SLPlayer[] accepted) {
                                    List<SpleefPlayer> players = new ArrayList<>();
                                    for (SLPlayer slpt : accepted) {
                                        players.add(SuperSpleef.getInstance().getPlayerManager().get(slpt));
                                    }
                                    arena.startBattle(players, StartReason.CHALLENGE);
                                }
                            };
                            success(p, "The players have been challenged.");
                            Collection<Player> bplayers = new ArrayList<>();
                            for (SLPlayer slpt : players) {
                                slpt.addChallenge(challenge);
                                bplayers.add(slpt.getPlayer());
                            }
                            challenge.sendMessages(SuperSpleef.getInstance().getChatPrefix(), arena.getName(), bplayers);
                        } else {
                            error(p, "This arena requires " + arena.getSize() + " players.");
                        }
                    } else {
                        if (TeamSpleefArena.byName(args[1]) != null) {
                            error(p, "The arena " + args[1] + " is for teamspleef. use /teamspleef instead");
                            return;
                        }
                        error(p, "The arena " + args[1] + " does not exist.");
                    }
                } else if (args.length > 0 && args[0].equalsIgnoreCase("points") && (slp.getRank() != null && slp.getRank().hasPermission(Rank.SENIOR_MODERATOR) || Collections.singletonList(Rank.MODERATOR).contains(slp.getRank()))) {
                    if (args.length != 4 || !(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                        p.sendMessage(plugin.getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + "Correct Usage: ");
                        p.sendMessage(plugin.getChatPrefix() + " " + Theme.INCOGNITO.buildTheme(false) + "/s points <add|remove> <player> <points>");
                        return;
                    }
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player == null) {
                        error(p, args[2] + " isn't online!");
                        return;
                    }
                    int points;
                    try {
                        points = Integer.valueOf(args[3]);
                    } catch (Exception e) {
                        error(p, "The points value must be a number!");
                        return;
                    }
                    SpleefPlayer spleefPlayer = SuperSpleef.getInstance().getPlayerManager().get(player);
                    if (args[1].equalsIgnoreCase("add")) {
                        spleefPlayer.setRating(spleefPlayer.getRating() + points);
                    } else {
                        spleefPlayer.setRating(spleefPlayer.getRating() - points);
                    }
                    success(p, "You have " + (args[1].equalsIgnoreCase("add") ? "added " : "removed ") + points + " points " + (args[1].equalsIgnoreCase("add") ? "to " : "from ") + player.getName() + "!");
                } else {
                    sendUsage(p);
                }
            } else {
                error(p, "You are currently ingame!");
            }
        } else {
            error(p, "All queues are currently paused!");
        }
    }
}
