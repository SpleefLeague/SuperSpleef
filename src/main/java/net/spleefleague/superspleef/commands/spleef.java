/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.commands;

import net.spleefleague.core.command.BasicCommand;
import net.spleefleague.core.io.EntityBuilder;
import net.spleefleague.core.player.SLPlayer;
import net.spleefleague.core.plugin.CorePlugin;
import net.spleefleague.core.plugin.GamePlugin;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Arena;
import net.spleefleague.superspleef.game.BattleManager;
import net.spleefleague.superspleef.game.signs.GameSign;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

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
        SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(p);
        BattleManager bm = SuperSpleef.getInstance().getBattleManager();
        if(!GamePlugin.isIngameGlobal(p)) {
            if(args.length == 0) {
                if(!GamePlugin.isQueuedGlobal(p)) {
                    bm.queue(sp);
                    success(p, "You have been added to the queue.");
                }
                else {
                    error(p, "You are already in a queue! Enter /leave to leave the queue.");
                }
            }
            else if(args.length == 1) {
                Arena arena = Arena.byName(args[0]);
                if(arena != null) {
                    if (!arena.isPaused()) {
                        if(sp.getVisitedArenas().contains(arena)) {
                            bm.queue(sp, arena);
                            success(p, "You have been added to the queue for: " + ChatColor.GREEN + arena.getName());
                        }
                        else {
                            error(p, "You have not visited this arena yet!");
                        }
                    } else {
                        error(p, "This arena is currently paused.");
                    }
                }
                else {
                    error(p, "This arena does not exist.");
                }
            }
            else if(args.length == 2) {
                Arena arena = Arena.byName(args[1]);
                if(arena != null) {
                    if(args[0].equalsIgnoreCase("pause")) {
                        arena.setPaused(true);
                        success(p, "You have paused the arena " + arena.getName());
                    }
                    else if(args[0].equalsIgnoreCase("unpause")) {
                        arena.setPaused(false);
                        success(p, "You have unpaused the arena " + arena.getName());
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
            error(p, "You are currently ingame!");
        }
    }
}
