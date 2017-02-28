/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.google.common.collect.Lists;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.events.BattleEndEvent;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author Jonas
 */
public class NormalSpleefBattle extends SpleefBattle {
    
    protected NormalSpleefBattle(Arena arena, List<SpleefPlayer> players) {
        super(arena, players);
        if(arena.getSpleefMode() == SpleefMode.MULTI && arena.getMaxRating() == -1) {
            this.changePointsCup((players.size() - 1) * 5);
        }
    }
    
    @Override
    public void removePlayer(SpleefPlayer sp, boolean surrender) {
        if (!surrender) {
            for (SpleefPlayer pl : getActivePlayers()) {
                pl.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
            for (SpleefPlayer pl : getSpectators()) {
                pl.sendMessage(SuperSpleef.getInstance().getChatPrefix() + " " + Theme.ERROR.buildTheme(false) + sp.getName() + " has left the game!");
            }
        }
        resetPlayer(sp);
        ArrayList<SpleefPlayer> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            end(activePlayers.get(0), surrender ? EndReason.SURRENDER : EndReason.QUIT);
        } else {
            getPlayers().remove(sp);
        }
    }
    
    private String getPlayToString() {
        return ChatColor.GOLD + "Playing to: ";
    }
    
    @Override
    public void onScoreboardUpdate() {
        reInitScoreboard();
    }
    
    private void reInitScoreboard() {
        if(getScoreboard() == null) return;
        getScoreboard().getObjective("rounds").unregister();
        Objective objective = getScoreboard().registerNewObjective("rounds", "dummy");
        String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
        objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        Set<String> requestingReset = new HashSet();
        Set<String> requestingEnd = new HashSet();
        for (SpleefPlayer sp : this.getPlayers()) {
            if (sp.isRequestingReset()) {
                requestingReset.add(sp.getName());
            }
            if (sp.isRequestingEndgame()) {
                requestingEnd.add(sp.getName());
            }
            objective.getScore(sp.getName()).setScore(getData(sp).getPoints());
        }
        if (!requestingEnd.isEmpty() || !requestingReset.isEmpty()) {
            objective.getScore(ChatColor.BLACK + "-----------").setScore(-1);
        }
        if (!requestingReset.isEmpty()) {
            objective.getScore(ChatColor.GOLD + "Reset requested").setScore(-2);
            for (String name : requestingReset) {
                objective.getScore(ChatColor.LIGHT_PURPLE + "> " + name).setScore(-3);
            }
        }
        if (!requestingEnd.isEmpty()) {
            objective.getScore(ChatColor.RED + "End requested").setScore(-4);
            for (String name : requestingEnd) {
                objective.getScore(ChatColor.AQUA + "> " + name).setScore(-5);
            }
        }
    }
    
    @Override
    protected void onStart() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team team = scoreboard.registerNewTeam("NO_COLLISION");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        for (SpleefPlayer sp : getPlayers()) {
            objective.getScore(sp.getName()).setScore(0);
            sp.setScoreboard(scoreboard);
            team.addEntry(sp.getName());
        }
        objective.getScore(getPlayToString()).setScore(getPlayTo());
        setScoreboard(scoreboard);        
    }
    
    @Override
    public void changePointsCup(int value) {
        super.changePointsCup(value);
        
        this.reInitScoreboard();
    }
    
    @Override
    public void end(SpleefPlayer winner, EndReason reason) {
        if(!this.getArena().isRated() && winner != null) {
            ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.RED + winner.getName() + Theme.INFO.buildTheme(false) + " has won the match!", getGameChannel());
        }
        Lists.newArrayList(getSpectators()).forEach(this::resetPlayer);
        Lists.newArrayList(getActivePlayers()).forEach((p) -> {
            this.resetPlayer(p);
            p.invalidatePlayToRequest();
        });
        saveGameHistory(winner);
        if (reason == EndReason.CANCEL) {
            if (reason == EndReason.CANCEL) {
                ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", getGameChannel());
            }
        } else if (reason != EndReason.ENDGAME) {
            if (getArena().isRated()) {
                applyRatingChange(winner);
            }
        }
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, reason));
        cleanup();
    }
    
    @Override
    public void startRound() {
        getActivePlayers().stream().forEach(sp -> {
            sp.setDead(false);
            sp.setGameMode(GameMode.ADVENTURE);
        });
        super.startRound();
    }
    
    @Override
    public void onArenaLeave(SpleefPlayer player) {
        if(player.isDead()) {
            return;
        }
        if (isInCountdown()) {
            player.teleport(getData(player).getSpawn());
        } 
        else {
            player.setDead(true);
            int maxScore = 0;
            SpleefPlayer winner = null;
            List<SpleefPlayer> alive = getActivePlayers().stream().filter(sp -> !sp.isDead()).collect(Collectors.toList());
            giveTempSpectator(player, alive.get(new Random().nextInt(alive.size())).getPlayer());
            for (SpleefPlayer sp : getActivePlayers()) {
                PlayerData playerdata = getData(sp);
                if (!sp.isDead()) {
                    playerdata.increasePoints();
                    getScoreboard().getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                }
                if (playerdata.getPoints() >= getPlayTo()) {
                        if (maxScore == playerdata.getPoints()) {
                            winner = null;
                        } else if (maxScore < playerdata.getPoints()) {                        
                            maxScore = playerdata.getPoints();
                            winner = sp;
                        }
                    }
            }
            if(winner == null && maxScore >= this.getPlayTo()) {
                changePointsCup(maxScore + 1);
            }
            if (alive.size() == 1 && winner != null) {
                end(winner, EndReason.NORMAL);
            } else {
                if (alive.size() == 1) {
                    setRound(getRound() + 1);
                    ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + alive.get(0).getName() + " has won round " + getRound(), getGameChannel());
                    startRound();
                }
            }
            if (getArena().getScoreboards() != null) {
                int[] score = new int[getArena().getSize()];
                int i = 0;
                for (SpleefPlayer sp : getActivePlayers()) {
                    score[i++] = getData(sp).getPoints();
                }
                for (com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : getArena().getScoreboards()) {
                    scoreboard.setScore(score);
                }
            }
        }
        reInitScoreboard();
    }
    
    private void giveTempSpectator(SpleefPlayer sp, Player target) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getSpectatorTarget() != null &&
                player.getSpectatorTarget().getUniqueId().equals(sp.getUniqueId())) {
                player.setSpectatorTarget(target);
            }
        }
        sp.getInventory().setArmorContents(new ItemStack[4]);
        sp.setGameMode(GameMode.SPECTATOR);
        sp.setSpectatorTarget(target);
    }
    
    @Override
    protected void updateScoreboardTime() {
        if (getScoreboard() != null) {
            Objective objective = getScoreboard().getObjective("rounds");
            if (objective != null) {
                String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
                objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
            }
        }
    }
    
    private void applyRatingChange(SpleefPlayer winner) {
        int winnerPoints = 0;
        final int MIN_RATING = 1, MAX_RATING = 40;
        int winnersIngamePoints = getData(winner).getPoints();
        boolean earnCoins = winnersIngamePoints >= 5;
        String endScore = winnersIngamePoints + "-";
        String playerList = "";
        for (SpleefPlayer sp : getPlayers()) {
            if (sp != winner) {
                float elo = (float) (1f / (1f + Math.pow(2f, ((sp.getRating() - winner.getRating()) / 250f))));
                int rating = (int) Math.round(MAX_RATING * (1f - elo));
                if (rating < MIN_RATING) {
                    rating = MIN_RATING;
                }
                winnerPoints += rating;
                sp.setRating(sp.getRating() - rating);
                playerList += ChatColor.RED + sp.getName() + ChatColor.WHITE + " (" + sp.getRating() + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + -rating + ChatColor.WHITE + " points. ";
                endScore += getData(sp).getPoints() + "-";
                if (earnCoins) {
                    SpleefLeague.getInstance().getPlayerManager().get(sp).changeCoins(2);
                }
            }
        }
        endScore = endScore.substring(0, endScore.length() - 1);
        winner.setRating(winner.getRating() + winnerPoints);
        if (earnCoins) {
            SpleefLeague.getInstance().getPlayerManager().get(winner).changeCoins(5);
        }
        playerList += ChatColor.RED + winner.getName() + ChatColor.WHITE + " (" + winner.getRating() + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + winnerPoints + ChatColor.GREEN + (winnerPoints == 1 ? " point. " : " points. ");
        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN + " is over " + ChatColor.WHITE + "(" + endScore + ")" + ChatColor.GREEN + ". " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
        this.getPlayers().forEach((p) -> {
            SuperSpleef.getInstance().getPlayerManager().save(p);
        });
    }
}
