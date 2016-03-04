/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.chat.ChatManager;
import com.spleefleague.core.chat.Theme;
import com.spleefleague.core.events.BattleEndEvent;
import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Jonas
 */
public class NormalSpleefBattle extends SpleefBattle {
    
    protected NormalSpleefBattle(Arena arena, List<SpleefPlayer> players) {
        super(arena, players);
    }
    
    @Override
    public void removePlayer(SpleefPlayer sp, boolean surrender) {
        if(!surrender) {
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
        }
    }

    @Override
    protected void onStart() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("rounds", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GRAY + "00:00:00 | " + ChatColor.RED + "Score:");
        for(SpleefPlayer sp : getPlayers()) {
            scoreboard.getObjective("rounds").getScore(sp.getName()).setScore(0);
            sp.setScoreboard(scoreboard);    
        }
        setScoreboard(scoreboard);
    }
    
    @Override
    public void end(SpleefPlayer winner, EndReason reason) {
        for (SpleefPlayer sp : new ArrayList<>(getSpectators())) {
            resetPlayer(sp);
        }
        for (SpleefPlayer sp : getActivePlayers()) {
            resetPlayer(sp);
        }
        saveGameHistory(winner);
        if(reason == EndReason.CANCEL) {
            if(reason == EndReason.CANCEL) {
                ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INCOGNITO.buildTheme(false) + "The battle has been cancelled by a moderator.", getGameChannel());
            }
        }
        else if(reason != EndReason.ENDGAME) {
            if(getArena().isRated()) {
                applyRatingChange(winner);
            }
        }
        Bukkit.getPluginManager().callEvent(new BattleEndEvent(this, reason));
        cleanup();
    }

    @Override
    public void onArenaLeave(SpleefPlayer player) {
        if (isInCountdown()) {
            player.teleport(getData(player).getSpawn());
        }
        else {
            for (SpleefPlayer sp : getActivePlayers()) {
                if (sp != player) {
                    PlayerData playerdata = getData(sp);
                    playerdata.increasePoints();
                    getScoreboard().getObjective("rounds").getScore(sp.getName()).setScore(playerdata.getPoints());
                    if (playerdata.getPoints() < getArena().getMaxRating()) {
                        setRound(getRound() + 1);
                        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), Theme.INFO.buildTheme(false) + sp.getName() + " has won round " + getRound(), getGameChannel());
                        startRound();
                    }
                    else {
                        end(sp, EndReason.NORMAL);
                    }
                }
                
            }
            if (getArena().getScoreboards() != null) {
                int[] score = new int[getArena().getSize()];
                int i = 0;
                for (SpleefPlayer sp : getActivePlayers()) {
                    score[i++] = getData(sp).getPoints();
                }
                for(com.spleefleague.superspleef.game.scoreboards.Scoreboard scoreboard : getArena().getScoreboards()) {
                    scoreboard.setScore(score);
                }
            }
        }
    }
    
    @Override
    protected void updateScoreboardTime() {
        if (getScoreboard() == null) {
            return;
        }
        Objective objective = getScoreboard().getObjective("rounds");
        if (objective != null) {
            String s = DurationFormatUtils.formatDuration(getTicksPassed() * 50, "HH:mm:ss", true);
            objective.setDisplayName(ChatColor.GRAY.toString() + s + " | " + ChatColor.RED + "Score:");
        }
    }

    private void applyRatingChange(SpleefPlayer winner) {
        int winnerPoints = 0;
        int winnerSWCPoints = 0;
        final int MIN_RATING = 1, MAX_RATING = 40;
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
            }
        }
        winner.setRating(winner.getRating() + winnerPoints);
        playerList += ChatColor.RED + winner.getName() + ChatColor.WHITE + " (" + winner.getRating() + ")" + ChatColor.GREEN + " gets " + ChatColor.GRAY + winnerPoints + ChatColor.GREEN + (winnerPoints == 1 ? " point. " : " points. ");
        ChatManager.sendMessage(SuperSpleef.getInstance().getChatPrefix(), ChatColor.GREEN + "Game in arena " + ChatColor.WHITE + getArena().getName() + ChatColor.GREEN + " is over. " + playerList, SuperSpleef.getInstance().getEndMessageChannel());
    }
}
