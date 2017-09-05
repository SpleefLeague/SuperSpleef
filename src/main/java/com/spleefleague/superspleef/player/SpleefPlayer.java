/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.player;

import com.spleefleague.core.queue.BattleManager;
import com.spleefleague.core.queue.RatedPlayer;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.powerspleef.Shovel;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jonas
 */
public class SpleefPlayer extends RatedPlayer {

    private int rating, playTo;
    private boolean ingame, frozen, requestingReset, requestingEndgame, dead;
    private Set<Arena> visitedArenas;
    private Set<String> visitedArenas_broken;
    @DBLoad(fieldName = "availableShovels")
    @DBSave(fieldName = "availableShovels")
    private Set<Shovel> availableShovels;
    
    public SpleefPlayer() {
        visitedArenas = new HashSet<>();
        visitedArenas_broken = new HashSet<>();
        setDefaults();
    }

    @DBLoad(fieldName = "rating")
    public void setRating(int rating) {
        this.rating = (rating > 0) ? rating : 0;
    }

    @Override
    @DBSave(fieldName = "rating")
    public int getRating() {
        return rating;
    }

    public int getRank() {
        return (int) SuperSpleef.getInstance().getPluginDB().getCollection("Players").count(new Document("rating", new Document("$gt", rating))) + 1;
    }

    @DBSave(fieldName = "visitedArenas")
    private List<String> saveVisitedArenas() {
        List<String> arenaNames = new ArrayList<>();
        for (Arena arena : visitedArenas) {
            if (arena != null) {
                arenaNames.add(arena.getName());
            }
        }
        for (String arena : visitedArenas_broken) {
            if (arena != null) {
                arenaNames.add(arena);
            }
        }
        return arenaNames;
    }

    @DBLoad(fieldName = "visitedArenas")
    private void loadVisitedArenas(List<String> arenaNames) {
        for (String name : arenaNames) {
            Arena arena = Arena.byName(name);
            if (arena != null) {
                visitedArenas.add(arena);
            } else {
                visitedArenas_broken.add(name);
            }
        }
    }

    public Set<Shovel> getAvailableShovels() {
        return availableShovels;
    }

    public void setIngame(boolean ingame) {
        this.ingame = ingame;
    }

    public boolean isIngame() {
        return ingame;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setRequestingReset(boolean requestingReset) {
        this.requestingReset = requestingReset;
    }

    public boolean isRequestingReset() {
        return requestingReset;
    }

    public void setRequestingEndgame(boolean requestingEndgame) {
        this.requestingEndgame = requestingEndgame;
    }

    public boolean isRequestingEndgame() {
        return requestingEndgame;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isDead() {
        return dead;
    }

    public SpleefBattle getCurrentBattle() {
        SpleefBattle battle = null;
        for(BattleManager<Arena, SpleefPlayer, SpleefBattle> bm : SuperSpleef.getInstance().getBattleManagers()) {
            battle = bm.getBattle(this);
            if(battle != null) {
                break;
            }
        }
        return battle;
    }

    public Set<Arena> getVisitedArenas() {
        return visitedArenas;
    }

    public int getPlayToRequest() {
        return playTo;
    }

    public void setPlayToRequest(int playTo) {
        this.playTo = playTo;
    }

    public void invalidatePlayToRequest() {
        this.playTo = -1;
    }

    @Override
    public void setDefaults() {
        super.setDefaults();
        this.rating = 1000;
        this.playTo = -1;
        this.frozen = false;
        this.ingame = false;
    }
}
