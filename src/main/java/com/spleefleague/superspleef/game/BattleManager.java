/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.queue.GameQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.signs.GameSign;
import com.spleefleague.superspleef.player.SpleefPlayer;

/**
 *
 * @author Jonas
 */
public class BattleManager {
    
    private final HashSet<Battle> activeBattles;
    private final GameQueue<SpleefPlayer, Arena> gameQueue;
    private final SpleefMode mode;
    
    public BattleManager(SpleefMode mode) {
        this.activeBattles = new HashSet<>();
        this.gameQueue = new GameQueue<>();
        this.mode = mode;
        for(Arena arena : Arena.getAll()) {
            if(mode == arena.getSpleefMode()) {
                gameQueue.register(arena);
            }
        }
    }
    
    public SpleefMode getMode() {
        return mode;
    }
    
    public GameQueue<SpleefPlayer, Arena> getGameQueue() {
        return gameQueue;
    }
    
    public void registerArena(Arena arena) {
        gameQueue.register(arena);
    }
    
    public void unregisterArena(Arena arena) {
        gameQueue.unregister(arena);
    }
    
    public void queue(SpleefPlayer player, Arena queue) {
        gameQueue.queue(player, queue);
        if(!queue.isPaused() && !queue.isOccupied()) {
            Collection<SpleefPlayer> players = gameQueue.request(queue);
            if(players != null) {
                queue.startBattle(new ArrayList<>(players));
            }
        }
        GameSign.updateGameSigns();
    }
    
    public void queue(SpleefPlayer player) {
        gameQueue.queue(player);
        HashMap<Arena, Collection<SpleefPlayer>> requested = gameQueue.request();
        for(Arena arena : requested.keySet()) {
            arena.startBattle(new ArrayList<>(requested.get(arena)));
        }
        GameSign.updateGameSigns();
    }
    
    public void dequeue(SpleefPlayer sp) {
        gameQueue.dequeue(sp);
        GameSign.updateGameSigns();
    }

    public boolean isQueued(SpleefPlayer sp) {
        return gameQueue.isQueued(sp);
    }
    
    public void add(Battle battle) {
        activeBattles.add(battle);
    }
    
    public void remove(Battle battle) {
        activeBattles.remove(battle);
        Collection<SpleefPlayer> players = gameQueue.request(battle.getArena());
        if(players != null) {
            battle.getArena().startBattle(new ArrayList<>(players));
        }
    }
    
    public Collection<Battle> getAll() {
        return (Collection<Battle>)activeBattles.clone();
    }
    
    public Battle getBattle(SpleefPlayer splayer) {
        for(Battle battle : activeBattles) {
            for(SpleefPlayer sp : battle.getActivePlayers()) {
                if(sp == splayer) {
                    return battle;
                }
            }
        }
        return null;
    }
    
    public Battle getBattle(Arena arena) {
        for(Battle battle : activeBattles) {
            if(battle.getArena() == arena) {
                return battle;
            }
        }
        return null;
    }
    
    public boolean isIngame(SpleefPlayer sjp) {
        return getBattle(sjp) != null;
    }
}