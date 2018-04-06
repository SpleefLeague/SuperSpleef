/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import static com.spleefleague.annotations.CommandSource.COMMAND_BLOCK;
import static com.spleefleague.annotations.CommandSource.CONSOLE;
import static com.spleefleague.annotations.CommandSource.PLAYER;
import com.spleefleague.annotations.Endpoint;
import com.spleefleague.annotations.LiteralArg;
import com.spleefleague.annotations.PlayerArg;
import com.spleefleague.annotations.StringArg;
import com.spleefleague.commands.command.BasicCommand;
import com.spleefleague.gameapi.events.BattleStartEvent.StartReason;
import com.spleefleague.core.player.DBPlayerManager;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.gameapi.GamePlugin;
import com.spleefleague.gameapi.queue.BattleManager;
import com.spleefleague.gameapi.queue.Challenge;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Jonas
 */
public abstract class spleefCommand<A extends Arena> extends BasicCommand {

    private final Function<String, A> getByName;
    private final BattleManager<A, SpleefPlayer, ? extends SpleefBattle> battleManager;
    
    protected spleefCommand(CorePlugin plugin, String name, String usage, Function<String, A> getByName, BattleManager<A, SpleefPlayer, ? extends SpleefBattle> battleManager) {
        super(SuperSpleef.getInstance(), new spleefCommandDispatcher(), name, usage);
        this.getByName = getByName;
        this.battleManager = battleManager;
    }

    private boolean checkQueuesClosed(CommandSender p) {
        if (!SuperSpleef.getInstance().queuesOpen()) {
            error(p, "All queues are currently paused!");
            return true;
        }
        return false;
    }

    private boolean checkIngame(CommandSender p) {
        if (p instanceof Player && GamePlugin.isIngameGlobal((Player)p)) {
            error(p, "You are currently ingame!");
            return true;
        }
        return false;
    }
    
    @Endpoint(target = {COMMAND_BLOCK})
    public void forceQueueGlobally(CommandSender sender, @LiteralArg("queue") String l, @PlayerArg Player target) {
        if (checkQueuesClosed(sender)) {
            return;
        }
        if (checkIngame(target)) {
            return;
        }
        GamePlugin.dequeueGlobal(target);
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(target);
        battleManager.queue(sjp);
        success(target, "You have been added to the queue");
    }
    
    @Endpoint(target = {COMMAND_BLOCK})
    public void forceQueueArena(CommandSender sender, @LiteralArg("queue") String l, @PlayerArg Player target, @StringArg String arenaName) {
        if (checkQueuesClosed(sender)) {
            return;
        }
        if (checkIngame(target)) {
            return;
        }
        A arena = getByName.apply(arenaName);
        if (arena == null) {
            error(target, "This arena does not exist.");
            return;
        }
        if (arena.isPaused()) {
            error(target, "This arena is currently paused.");
            return;
        }
        SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(target);
        if (!arena.isAvailable(sjp)) {
            error(target, "You have not visited this arena yet!");
            return;
        }
        battleManager.queue(sjp, arena);
        success(target, "You have been added to the queue for: " + ChatColor.GREEN + arena.getName());
    }

    @Endpoint(target = {PLAYER})
    public void queueGlobally(Player sender) {
        forceQueueGlobally(sender, "queue", sender);
    }

    @Endpoint(target = {PLAYER})
    public void queueArena(Player sender, @StringArg String arenaName) {
        forceQueueArena(sender, "queue", sender, arenaName);
    }

    @Endpoint(target = {PLAYER, CONSOLE, COMMAND_BLOCK})
    public void forcestart(CommandSender sender, @LiteralArg(value = "match", aliases = {"m"}) String l, @StringArg String arenaName, @PlayerArg Player[] players) {
        if (checkIngame(sender)) {
            return;
        }
        if(sender instanceof SLPlayer) {
            SLPlayer slp = (SLPlayer)sender;
            if (!slp.getRank().hasPermission(Rank.MODERATOR) && slp.getRank() != Rank.ORGANIZER) {
                sendUsage(sender);
                return;
            }
        }
        A arena = getByName.apply(arenaName);
        if (arena == null) {
            error(sender, "This arena does not exist.");
            return;
        }
        if (arena.isOccupied()) {
            error(sender, "This arena is currently occupied.");
            return;
        }
        if (arena.getRequiredPlayers() > players.length || arena.getSize() < players.length) {
            if (arena.getRequiredPlayers() == arena.getSize()) {
                error(sender, "This arena requires " + arena.getSize() + " players.");
            } else {
                error(sender, "This arena requires between " + arena.getRequiredPlayers() + " and " + arena.getSize() + " players");
            }
            return;
        }
        DBPlayerManager<SpleefPlayer> pm = SuperSpleef.getInstance().getPlayerManager();
        List<SpleefPlayer> SpleefPlayers = Arrays.stream(players)
                .map(pm::get)
                .collect(Collectors.toList());
        arena.startBattle(SpleefPlayers, StartReason.FORCE);
        success(sender, "You started a battle on the arena " + arena.getName());
    }
    
    @Endpoint(target = {PLAYER})
    public void challenge(SLPlayer sender, @LiteralArg(value = "challenge", aliases = {"c"}) String l, @StringArg String arenaName, @PlayerArg Player[] players) {
        if (checkQueuesClosed(sender)) {
            return;
        }
        if (checkIngame(sender)) {
            return;
        }
        A arena = getByName.apply(arenaName);
        if (arena == null) {
            error(sender, "This arena does not exist.");
            return;
        }
        if (arena.isPaused()) {
            error(sender, "This arena is currently paused.");
            return;
        }
        if (arena.isOccupied()) {
            error(sender, "This arena is currently occupied.");
            return;
        }
        if (arena.getRequiredPlayers() - 1 > players.length || arena.getSize() - 1 < players.length) {
            if (arena.getRequiredPlayers() == arena.getSize()) {
                error(sender, "This arena requires " + arena.getSize() + " players.");
            } else {
                error(sender, "This arena requires between " + arena.getRequiredPlayers() + " and " + arena.getSize() + " players");
            }
            return;
        }
        DBPlayerManager<SpleefPlayer> pm = SuperSpleef.getInstance().getPlayerManager();
        SpleefPlayer challenger = pm.get(sender);
        if (sender.getState() == PlayerState.INGAME) {
            error(sender, "You are currently ingame.");
            return;
        }
        if (!arena.isAvailable(challenger)) {
            error(sender, "You have not discovered this arena yet.");
        }
        List<SpleefPlayer> challenged = new ArrayList<>();
        for (Player player : players) {
            SpleefPlayer sp = pm.get(player);
            if (sp == challenger) {
                error(sender, "You cannot challenge yourself.");
                return;
            }
            if (challenged.contains(sp)) {
                error(sender, "You cannot challenge " + sp.getName() + " more than once.");
                return;
            }
            if (GamePlugin.isIngameGlobal(player)) {
                error(sender, player.getName() + " is currently ingame.");
                return;
            }
            if (!arena.isAvailable(sp)) {
                error(sender, player.getName() + " has not discovered this arena yet.");
            }
            challenged.add(sp);
        }
        Challenge<SpleefPlayer> challenge = new Challenge<SpleefPlayer>(challenger, challenged) {
            @Override
            public void start(List<SpleefPlayer> accepted) {
                arena.startBattle(accepted, StartReason.CHALLENGE);
            }
        };
        challenge.sendMessages(SuperSpleef.getInstance().getChatPrefix(), arena.getName(), Arrays.asList(players));
        success(sender, "The players have been challenged.");
    }

    @Endpoint(target = {PLAYER, CONSOLE})
    public void pause(CommandSender sender, @LiteralArg(value = "pause") String l, @StringArg String arenaName) {
        handlePause(sender, true, arenaName);
    }

    @Endpoint(target = {PLAYER, CONSOLE})
    public void unpause(CommandSender sender, @LiteralArg(value = "unpause") String l, @StringArg String arenaName) {
        handlePause(sender, false, arenaName);
    }

    private void handlePause(CommandSender sender, boolean pauseValue, @StringArg String arenaName) {
        if(sender instanceof SLPlayer) {
            SLPlayer slp = (SLPlayer)sender;
            if (!slp.getRank().hasPermission(Rank.MODERATOR) && slp.getRank() != Rank.ORGANIZER) {
                sendUsage(sender);
                return;
            }
        }
        A arena = getByName.apply(arenaName);
        if (arena == null) {
            error(sender, "This arena does not exist.");
            return;
        }
        arena.setPaused(pauseValue);
        if (pauseValue) {
            success(sender, "You have paused the arena " + arena.getName());
        } else {
            success(sender, "You have unpaused the arena " + arena.getName());
        }
    }
}
