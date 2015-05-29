/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.player;

import net.spleefleague.core.io.DBLoad;
import net.spleefleague.core.io.DBSave;
import net.spleefleague.core.player.GeneralPlayer;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Battle;

/**
 *
 * @author Jonas
 */
public class SpleefPlayer extends GeneralPlayer {
    
    private int rating, swcRating;
    private boolean ingame, frozen, requestingReset, joinedSWC;
    
    public SpleefPlayer() {
        super();
    }
    
    @DBLoad(fieldName = "rating")
    public void setRating(int rating) {
        this.rating = (rating > 0) ? rating : 0;
    }
    
    @DBSave(fieldName = "rating")
    public int getRating() {
        return rating;
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
    
    public Battle getCurrentBattle() {
        return SuperSpleef.getInstance().getBattleManager().getBattle(this);
    }
    
    @Override
    public void setDefaults() {
        super.setDefaults();
        this.rating = 1000;
        this.swcRating = 1000;
        this.frozen = false;
        this.ingame = false;
        this.joinedSWC = false;
    }
}
