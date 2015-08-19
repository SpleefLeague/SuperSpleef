/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.player;

import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.DBSave;
import com.spleefleague.core.io.Settings;
import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.Battle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;


/**
 *
 * @author Jonas
 */
public class SpleefPlayer extends GeneralPlayer {
    
    private int rating, swcRating;
    private boolean ingame, frozen, requestingReset, requestingEndgame, joinedSWC;
    private Set<Arena> visitedArenas;
    
    public SpleefPlayer() {
        setDefaults();
    }
    
    @DBLoad(fieldName = "rating")
    public void setRating(int rating) {
        this.rating = (rating > 0) ? rating : 0;
    }
    
    @DBSave(fieldName = "rating")
    public int getRating() {
        return rating;
    }
    
    public int getRank() {
        return (int)SuperSpleef.getInstance().getPluginDB().getCollection("Players").count(new Document("rating", new Document("$gt", rating))) + 1;
    }
    
    @DBLoad(fieldName = "joinedSWC")
    public void setJoinedSWC(boolean joinedSWC) {
        this.joinedSWC = joinedSWC;
    }
    
    @DBSave(fieldName = "joinedSWC")
    public boolean joinedSWC() {
        return joinedSWC;
    }
    
    @DBLoad(fieldName = "swcRating")
    public void setSwcRating(int swcRating) {
        this.swcRating = (swcRating > 0) ? swcRating : 0;
    }
    
    @DBSave(fieldName = "swcRating")
    public int getSwcRating() {
        return swcRating;
    }
    
    @DBSave(fieldName = "visitedArenas")
    private List<String> saveVisitedArenas() {
        List<String> arenaNames = new ArrayList<>();
        for(Arena arena : visitedArenas) {
            arenaNames.add(arena.getName());
        }
        return arenaNames;
    }
    
    @DBLoad(fieldName = "visitedArenas")
    private void loadVisitedArenas(List<String> arenaNames) {
        for(String name : arenaNames) {
            Arena arena = Arena.byName(name);
            if(arena != null) visitedArenas.add(arena);
        }
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
    
    public Battle getCurrentBattle() {
        if(SuperSpleef.getInstance().getBattleManagerSpleef().isIngame(this)) {  
            return SuperSpleef.getInstance().getBattleManagerSpleef().getBattle(this);
        }
        return SuperSpleef.getInstance().getBattleManagerMultiSpleef().getBattle(this);
    }
    
    public Set<Arena> getVisitedArenas() {
        return visitedArenas;
    }
    
    @Override
    public void setDefaults() {
        super.setDefaults();
        this.rating = 1000;
        this.swcRating = 1000;
        this.frozen = false;
        this.ingame = false;
        this.joinedSWC = false;
        visitedArenas = new HashSet<>();
        for(String name : (List<String>)Settings.getList("default_arenas_spleef")) {
            visitedArenas.add(Arena.byName(name));
        }
    }
}
