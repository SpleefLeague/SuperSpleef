/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.player;

import com.spleefleague.gameapi.queue.BattleManager;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.TypeConverter;
import com.spleefleague.gameapi.player.RatedPlayer;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.game.SpleefBattle;
import com.spleefleague.superspleef.game.SpleefMode;
import com.spleefleague.superspleef.cosmetics.Shovel;
import com.spleefleague.superspleef.game.power.PowerType;
import java.util.ArrayList;
import java.util.HashMap;
import org.bson.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Jonas
 */
public class SpleefPlayer extends RatedPlayer<SpleefMode> {

    private int playTo;
    private boolean ingame, frozen, requestingReset, requestingEndgame, dead;
    private Set<Arena> visitedArenas;
    private Set<Shovel> unlockedShovels;
    @DBSave(fieldName = "activeShovel")
    @DBLoad(fieldName = "activeShovel")
    private short activeShovel;
    @DBLoad(fieldName = "activePower")
    @DBSave(fieldName = "activePower")
    private PowerType activePower;
    
    public SpleefPlayer() {
        super(SpleefMode.class, SuperSpleef.getInstance().getPluginDB().getCollection("Players"));
        this.visitedArenas = new HashSet<>();
        setDefaults();
    }
    
    public Set<Shovel> getAvailableShovels() {
        return unlockedShovels;
    }
    
    public Shovel getActiveShovel() {
        Shovel shovel = Shovel.byDamageValue(activeShovel);
        if(shovel == null) {
            shovel = Shovel.DEFAULT_SHOVEL;
        }
        return shovel;
    }

    public PowerType getPowerType() {
        return activePower;
    }

    public void setActiveShovel(Shovel activeShovel) {
        this.activeShovel = activeShovel.getDamage();
    }

    public void setActivePower(PowerType activePower) {
        this.activePower = activePower;
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

    @Override
    public boolean isDead() {
        return dead;
    }

    public SpleefBattle<?> getCurrentBattle() {
        SpleefBattle battle = null;
        for(BattleManager<? extends Arena, SpleefPlayer, ? extends SpleefBattle> bm : SuperSpleef.getInstance().getBattleManagers()) {
            battle = bm.getBattle(this);
            if(battle != null) {
                break;
            }
        }
        return battle;
    }
    
    @DBSave(fieldName = "availableShovels")
    private List<Short> saveAvailableShovels() {
        List<Short> shovelIds = new ArrayList<>();
        for (Shovel shovel : unlockedShovels) {
            shovelIds.add(shovel.getDamage());
        }
        return shovelIds;
    }

    @DBLoad(fieldName = "availableShovels")
    private void loadAvailableShovels(List<Short> shovelIds) {
        for (Short shovelId : shovelIds) {
            Shovel shovel = Shovel.byDamageValue(shovelId);
            if (shovel != null) {
                unlockedShovels.add(shovel);
            }
        }
    }

    @DBSave(fieldName = "visitedArenas")
    private List<Document> saveVisitedArenas() {
        List<Document> arenaNames = new ArrayList<>();
        for (Arena arena : visitedArenas) {
            arenaNames.add(new Document("name", arena.getName()).append("mode", arena.getSpleefMode().name()));
        }
        return arenaNames;
    }

    @DBLoad(fieldName = "visitedArenas")
    private void loadVisitedArenas(List<Document> arenas) {
        for (Document arenaDoc : arenas) {
            try {
                SpleefMode mode = SpleefMode.valueOf(arenaDoc.get("mode", String.class));
                Arena arena = Arena.byName(arenaDoc.get("name", String.class), mode);
                if (arena != null) {
                    visitedArenas.add(arena);
                }
            } catch(IllegalArgumentException e) {}
        }
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
        this.activeShovel = Shovel.DEFAULT_SHOVEL.getDamage();
        this.activePower = PowerType.EMPTY_POWER;
        this.unlockedShovels = new HashSet<>();
        this.playTo = -1;
        this.frozen = false;
        this.ingame = false;
    }
    
    public static class Rating {
        
        private final Map<SpleefMode, Integer> rating;
        
        public Rating() {
            this.rating = new HashMap<>();
        }
        
        public boolean isRated(SpleefMode mode) {
            return rating.containsKey(mode);
        }
        
        public int getRating(SpleefMode mode) {
            return rating.getOrDefault(mode, 1000);
        }
        
        public void setRating(SpleefMode mode, int rating) {
            if(mode == null) return;
            this.rating.put(mode, rating);
        }
    
        public static class RatingTypeConverter extends TypeConverter<List<Document>, Rating> {

            @Override
            public Rating convertLoad(List<Document> t) {
                Rating ratingData = new Rating();
                for(Document doc : t) {
                    try {
                        SpleefMode mode = SpleefMode.valueOf(doc.get("mode", String.class));
                        int rating = doc.get("rating", Integer.class);
                        ratingData.setRating(mode, rating);
                    } catch(Exception e) {
                        System.out.println("Failed to load rating data:\n" + doc);
                        e.printStackTrace();
                    }
                }
                return ratingData;
            }

            @Override
            public List<Document> convertSave(Rating v) {
                return v.rating
                        .entrySet()
                        .stream()
                        .map(e -> new Document("mode", e.getKey().name()).append("rating", e.getValue()))
                        .collect(Collectors.toList());
            }
        }
    }
}
