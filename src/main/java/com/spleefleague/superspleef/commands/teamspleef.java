/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.Challenge;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.TeamSpleefArena;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Jonas
 */
public class teamspleef extends BasicCommand {

    public teamspleef(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage, Rank.DEFAULT);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if (SuperSpleef.getInstance().queuesOpen()) {
            if (!GamePlugin.isIngameGlobal(p)) {
                if (args.length >= 2 && args[0].equalsIgnoreCase("match")) {
                    if (slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
                        TeamSpleefArena arena = TeamSpleefArena.byName(args[1]);
                        if (!arena.isOccupied()) {
                            if ((args.length - 2) == arena.getSize()) {
                                ArrayList<SpleefPlayer> players = new ArrayList<>();
                                for (int i = 0; i < args.length - 2; i++) {
                                    Player pl = Bukkit.getPlayerExact(args[i + 2]);
                                    if (pl != null) {
                                        SpleefPlayer n = SuperSpleef.getInstance().getPlayerManager().get(pl);
                                        SLPlayer splayer = SpleefLeague.getInstance().getPlayerManager().get(pl);
                                        if (players.contains(n)) {
                                            error(p, pl.getName() + " cannot be added more than once");
                                            return;
                                        }
                                        if (splayer.getState() == PlayerState.INGAME) {
                                            error(p, pl.getName() + " is already in a game");
                                            return;
                                        }
                                        players.add(n);
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
                        sendUsage(p);
                    }
                } else if (args.length == 2) {
                    if (slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
                        TeamSpleefArena arena = TeamSpleefArena.byName(args[1]);
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
                } else if (args.length >= 3 && args[0].equalsIgnoreCase("challenge")) {
                    TeamSpleefArena arena = TeamSpleefArena.byName(args[1]);
                    if (arena != null) {
                        if (args.length - 1 == arena.getSize()) {
                            Collection<SLPlayer> players = new ArrayList<>();
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
                                    if (players.contains(splayer)) {
                                        error(p, t.getName() + " cannot be added twice");
                                        return;
                                    }
                                    players.add(splayer);

                                } else {
                                    error(p, "The player " + args[i] + " is not online.");
                                    return;
                                }
                            }
                            Challenge challenge = new Challenge(slp, arena.getSize()) {
                                @Override
                                public void start(Collection<SLPlayer> accepted) {
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
                        error(p, "The arena " + args[1] + " does not exist.");
                    }
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
