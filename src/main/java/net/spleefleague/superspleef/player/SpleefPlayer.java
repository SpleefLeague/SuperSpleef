/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.player;

import com.mongodb.client.FindIterable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.spleefleague.core.io.DBLoad;
import net.spleefleague.core.io.DBSave;
import net.spleefleague.core.io.Settings;
import net.spleefleague.core.player.GeneralPlayer;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Arena;
import net.spleefleague.superspleef.game.Battle;
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
            visitedArenas.add(Arena.byName(name));
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
        return SuperSpleef.getInstance().getBattleManager().getBattle(this);
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
