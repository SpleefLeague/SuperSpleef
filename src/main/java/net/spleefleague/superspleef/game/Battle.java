/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.spleefleague.core.SpleefLeague;
import net.spleefleague.core.chat.Theme;
import net.spleefleague.core.player.PlayerState;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.player.SpleefPlayer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

/**
 *
 * @author Jonas
 */
public class Battle {
    
    private final Arena arena;
    private final List<SpleefPlayer> players; //MIGHT CONTAIN PLAYERS WHICH LEFT THE GAME. USE getActivePlayers() FOR ACTIVE PLAYERS INSTEAD
    private int ticksPassed = 0;
    private BukkitRunnable clock;
    private Scoreboard scoreboard;
    
    protected Battle(Arena arena, List<SpleefPlayer> players) {
        this.arena = arena;
        this.players = players;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public Collection<SpleefPlayer> getPlayers() {
        return players;
    }
    
    public void removePlayer(SpleefPlayer sjp) {
        resetPlayer(sjp);
        if(players.size() == 1) {
            end(players.get(0));
        }
        else if(players.size() > 1) {   
            for(SpleefPlayer pl : getActivePlayers()) {
                pl.getPlayer().sendMessage(SuperSpleef.getInstance().getPrefix() + " " + Theme.ERROR.buildTheme(false) + sjp.getName() + " has left the game!");
            }
        }
    }
    
    public ArrayList<SpleefPlayer> getActivePlayers() {
        ArrayList<SpleefPlayer> active = new ArrayList<>();
        for(SpleefPlayer sp : players) {
            if(sp.isIngame()) {
                active.add(sp);
            }
        }
        return active;
    }
    
    private void resetPlayer(SpleefPlayer sjp) {
        sjp.getPlayer().teleport(SpleefLeague.DEFAULT_WORLD.getSpawnLocation());
        sjp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        sjp.setIngame(false);
        sjp.setFrozen(false);
        SpleefLeague.getInstance().getPlayerManager().get(sjp.getPlayer()).setState(PlayerState.IDLE);
    }
    
    public void end(SpleefPlayer winner) {
        
    }
    
    public void end(SpleefPlayer winner, boolean rated) {
        
    }
    
    public void cancel() {
        
    }
    
    public void start() {
        
    }
    
    private void applyRatingChange(SpleefPlayer winner) {
        int winnerPoints = 0;
        final int MIN_RATING = 1, MAX_RATING = 20;
        for(SpleefPlayer sp : players) {
            if(sp != winner) {
                float elo = (float) (1f / (1f + Math.pow(2f, ((sp.getRating() - winner.getRating()) / 400f))));
                int rating = (int) Math.round(MAX_RATING * (1f - elo));
                if (rating < MIN_RATING) {
                    rating = MIN_RATING;
                }
                winnerPoints += rating;
                Bukkit.broadcastMessage(sp.getName() + ":" + -rating);
                sp.setRating(sp.getRating() - rating);
            }
        }
        Bukkit.broadcastMessage(winner.getName() + ":" + winnerPoints);
        winner.setRating(winner.getRating() + winnerPoints);
    }
}
