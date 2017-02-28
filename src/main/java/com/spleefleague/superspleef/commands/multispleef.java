/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import com.spleefleague.core.command.BasicCommand;
import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.BattleManager;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 *
 * @author Jonas
 */
public class multispleef extends BasicCommand {

    public multispleef(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), name, usage);
    }

    @Override
    protected void run(Player p, SLPlayer slp, Command cmd, String[] args) {
        if (SuperSpleef.getInstance().queuesOpen()) {
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
            BattleManager bm = SuperSpleef.getInstance().getMultiSpleefBattleManager();
            if (!GamePlugin.isIngameGlobal(p)) {
                if (args.length == 0) {
                    GamePlugin.dequeueGlobal(p);
                    bm.queue(sp);
                    success(p, "You have been added to the queue.");
                }
                else if (args.length == 1) {
                    Arena arena = Arena.byName(args[0]);
                    if (arena != null && arena.getSpleefMode() == SpleefMode.MULTI) {
                        if (!arena.isPaused()) {
                            if (sp.getVisitedArenas().contains(arena)) {
                                bm.queue(sp, arena);
                                success(p, "You have been added to the queue for: " + ChatColor.GREEN + arena.getName());
                            }
                            else {
                                error(p, "You have not visited this arena yet!");
                            }
                        }
                        else {
                            error(p, "This arena is currently paused.");
                        }
                    }
                    else {
                        error(p, "This arena does not exist.");
                    }
                }
                else if (args.length >= 2 && args[0].equalsIgnoreCase("match")) {
                    if(slp.getRank().hasPermission(Rank.MODERATOR) || slp.getRank() == Rank.ORGANIZER) {
                        Arena arena = Arena.byName(args[1]);
                        if(arena != null && arena.getSpleefMode() == SpleefMode.MULTI) {
                            if(!arena.isOccupied()) {
                                if((args.length - 2) >= arena.getRequiredPlayers() && (args.length - 2) <= arena.getSize()) {
                                    ArrayList<SpleefPlayer> players = new ArrayList<>();
                                    for(int i = 0; i < args.length - 2; i++) {
                                        Player pl = Bukkit.getPlayerExact(args[i + 2]);
                                        if(pl != null) {
                                            players.add(SuperSpleef.getInstance().getPlayerManager().get(pl));
                                        }
                                        else {
                                            error(p, "The player " + args[i + 2] + " is currently not online.");
                                            return;
                                        }
                                    }
                                    arena.startBattle(players, BattleStartEvent.StartReason.FORCE);
                                    success(p, "You started a battle on the arena " + arena.getName());
                                }
                                else {
                                    error(p, "You need to list " + (args.length - 2) + " players for this arena.");
                                }
                            }
                            else {
                                error(p, "This arena is currently occupied.");
                            }
                        }
                        else {
                            error(p, "This arena does not exist.");
                        }
                    }
                    else {
                        sendUsage(p);
                    }
                }
                else if (args.length == 2) {
                    if(slp.getRank().hasPermission(Rank.MODERATOR)) {
                        Arena arena = Arena.byName(args[1]);
                        if (arena != null && arena.getSpleefMode() == SpleefMode.MULTI) {
                            if (args[0].equalsIgnoreCase("pause")) {
                                arena.setPaused(true);
                                success(p, "You have paused the arena " + arena.getName());
                            }
                            else if (args[0].equalsIgnoreCase("unpause")) {
                                arena.setPaused(false);
                                success(p, "You have unpaused the arena " + arena.getName());
                            }
                            else {
                                sendUsage(p);
                            }
                            GameSign.updateGameSigns(arena);
                            EntityBuilder.save(arena, SuperSpleef.getInstance().getPluginDB().getCollection("Arenas"));
                        }
                        else {
                            error(p, "This arena does not exist.");
                        }
                    }
                    else {
                        sendUsage(p);
                    }
                }
                else {
                    sendUsage(p);
                }
            }
            else {
                error(p, "You are currently ingame!");
            }
        }
        else {
            error(p, "All queues are currently paused!");
        }
    }
}
