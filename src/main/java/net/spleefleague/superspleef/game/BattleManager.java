/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import net.spleefleague.core.queue.GameQueue;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.player.SpleefPlayer;

/**
 *
 * @author Jonas
 */
public class BattleManager {
    
    private final Collection<Battle> activeBattles;
    private final GameQueue<SpleefPlayer, Arena> gameQueue;
    
    public BattleManager() {
        this.activeBattles = new HashSet<>();
        this.gameQueue = new GameQueue<>(SuperSpleef.getInstance(), SuperSpleef.getInstance().getPlayerManager());
        for(Arena arena : Arena.getAll()) {
            gameQueue.register(arena);
        }
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
        gameQueue.queue(player, queue, queue.isQueued());
        if(!queue.isOccupied()) {
            Collection<SpleefPlayer> players = gameQueue.request(queue);
            if(players != null) {
                queue.startBattle(new ArrayList<>(players));
            }
        }
    }
    
    public void queue(SpleefPlayer player) {
        gameQueue.queue(player);
        HashMap<Arena, Collection<SpleefPlayer>> requested = gameQueue.request();
        for(Arena arena : requested.keySet()) {
            arena.startBattle(new ArrayList<>(requested.get(arena)));
        }
    }
    
    public void dequeue(SpleefPlayer sp) {
        gameQueue.dequeue(sp);
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
        return activeBattles;
    }
    
    public Battle getBattle(SpleefPlayer splayer) {
        for(Battle battle : activeBattles) {
            for(SpleefPlayer sp : battle.getPlayers()) {
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