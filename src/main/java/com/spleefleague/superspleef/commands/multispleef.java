/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.commands;

import static com.spleefleague.annotations.CommandSource.COMMAND_BLOCK;
import static com.spleefleague.annotations.CommandSource.PLAYER;
import com.spleefleague.annotations.Endpoint;
import com.spleefleague.annotations.LiteralArg;
import com.spleefleague.annotations.PlayerArg;
import com.spleefleague.annotations.StringArg;
import com.spleefleague.commands.command.BasicCommand;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.player.PlayerState;
import com.spleefleague.core.player.Rank;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.core.plugin.GamePlugin;
import com.spleefleague.core.queue.Challenge;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.multispleef.MultiSpleefArena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Jonas
 */
public class multispleef extends BasicCommand {

    public multispleef(CorePlugin plugin, String name, String usage) {
        super(SuperSpleef.getInstance(), new multispleefDispatcher(), name, usage);
    }

    
    private boolean checkQueuesClosed(CommandSender p) {
        if (!SuperSpleef.getInstance().queuesOpen()) {
            error(p, "All queues are currently paused!");
            return true;
        }
        return false;
    }

    private boolean checkIngame(Player p) {
        if (GamePlugin.isIngameGlobal(p)) {
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
        SuperSpleef.getInstance().getMultiSpleefBattleManager().queue(sjp);
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
        MultiSpleefArena arena = MultiSpleefArena.byName(arenaName);
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
        SuperSpleef.getInstance().getMultiSpleefBattleManager().queue(sjp, arena);
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

    @Endpoint(target = {PLAYER})
    public void forcestart(SLPlayer sender, @LiteralArg(value = "match") String l, @StringArg String arenaName, @PlayerArg Player[] players) {
        if (checkIngame(sender)) {
            return;
        }
        if (!sender.getRank().hasPermission(Rank.MODERATOR) && sender.getRank() != Rank.ORGANIZER) {
            sendUsage(sender);
            return;
        }
        MultiSpleefArena arena = MultiSpleefArena.byName(arenaName);
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
        PlayerManager<SpleefPlayer> pm = SuperSpleef.getInstance().getPlayerManager();
        List<SpleefPlayer> SpleefPlayers = Arrays.stream(players)
                .map(pm::get)
                .collect(Collectors.toList());
        arena.startBattle(SpleefPlayers, BattleStartEvent.StartReason.FORCE);
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
        MultiSpleefArena arena = MultiSpleefArena.byName(arenaName);
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
        PlayerManager<SpleefPlayer> pm = SuperSpleef.getInstance().getPlayerManager();
        SpleefPlayer sendersjp = pm.get(sender);
        if (sender.getState() == PlayerState.INGAME) {
            error(sender, "You are currently ingame.");
            return;
        }
        if (!arena.isAvailable(sendersjp)) {
            error(sender, "You have not discovered this arena yet.");
        }
        List<SLPlayer> challenged = new ArrayList<>();
        for (Player player : players) {
            SpleefPlayer sjp = pm.get(player);
            SLPlayer slp = SpleefLeague.getInstance().getPlayerManager().get(player);
            if (sjp == sendersjp) {
                error(sender, "You cannot challenge yourself.");
                return;
            }
            if (challenged.contains(slp)) {
                error(sender, "You cannot challenge " + sjp.getName() + " more than once.");
                return;
            }
            if (slp.getState() == PlayerState.INGAME) {
                error(sender, player.getName() + " is currently ingame.");
                return;
            }
            if (!arena.isAvailable(sjp)) {
                error(sender, player.getName() + " has not discovered this arena yet.");
            }
            challenged.add(slp);
        }
        Challenge challenge = new Challenge(sender, challenged.toArray(new SLPlayer[0])) {
            @Override
            public void start(SLPlayer[] accepted) {
                List<SpleefPlayer> players = new ArrayList<>();
                for (SLPlayer slpt : accepted) {
                    players.add(SuperSpleef.getInstance().getPlayerManager().get(slpt));
                }
                arena.startBattle(players, BattleStartEvent.StartReason.CHALLENGE);
            }
        };
        challenged.forEach((slpt) -> {
            slpt.addChallenge(challenge);
        });
        challenge.sendMessages(SuperSpleef.getInstance().getChatPrefix(), arena.getName(), Arrays.asList(players));
        success(sender, "The players have been challenged.");
    }

    @Endpoint(target = {PLAYER})
    public void pause(SLPlayer sender, @LiteralArg(value = "pause") String l, @StringArg String arenaName) {
        handlePause(sender, true, arenaName);
    }

    @Endpoint(target = {PLAYER})
    public void unpause(SLPlayer sender, @LiteralArg(value = "unpause") String l, @StringArg String arenaName) {
        handlePause(sender, false, arenaName);
    }

    private void handlePause(SLPlayer sender, boolean pauseValue, @StringArg String arenaName) {
        if (!sender.getRank().hasPermission(Rank.MODERATOR) && sender.getRank() != Rank.ORGANIZER) {
            sendUsage(sender);
            return;
        }
        MultiSpleefArena arena = MultiSpleefArena.byName(arenaName);
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
