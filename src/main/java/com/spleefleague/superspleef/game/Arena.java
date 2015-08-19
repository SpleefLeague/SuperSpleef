/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.mongodb.client.MongoCursor;
import com.spleefleague.core.io.DBEntity;
import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.DBLoadable;
import com.spleefleague.core.io.DBSave;
import com.spleefleague.core.io.DBSaveable;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.io.TypeConverter;
import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.core.queue.QueueableArena;
import com.spleefleague.core.utils.Area;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 *
 * @author Jonas
 */
public class Arena extends DBEntity implements DBLoadable, DBSaveable, QueueableArena{
    
    @DBLoad(fieldName = "border")
    private Area border;
    @DBLoad(fieldName = "spawns", typeConverter = TypeConverter.LocationConverter.class)
    private Location[] spawns;
    @DBLoad(fieldName = "creator")
    private String creator;
    @DBLoad(fieldName = "name")
    private String name;
    @DBLoad(fieldName = "rated")
    private boolean rated = true;
    @DBLoad(fieldName = "queued")
    private boolean queued = true;
    @DBLoad(fieldName = "tpBackSpectators")
    private boolean tpBackSpectators = true;
    @DBLoad(fieldName = "paused")
    @DBSave(fieldName = "paused")
    private boolean paused = false;
    @DBLoad(fieldName = "spectatorSpawn", typeConverter = TypeConverter.LocationConverter.class)
    private Location spectatorSpawn; //null -> default world spawn
    @DBLoad(fieldName = "maxRating")
    private int maxRating = 5;
    @DBLoad(fieldName = "area")
    private Area area;
    @DBLoad(fieldName = "spleefMode")
    private SpleefMode spleefMode = SpleefMode.NORMAL;
    private boolean occupied = false;
    
    public Arena() {
        
    }
    
    public Location[] getSpawns() {
        return spawns;
    }
    
    public Area getBorder() {
        return border;
    }
    
    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }
    
    public String getCreator() {
        return creator;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isOccupied() {
        return occupied;
    }
    
    public Area getArea() {
        return area;
    }
    
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
    
    public boolean isRated() {
        return rated;
    }
    
    public boolean isTpBackSpectators() {
        return tpBackSpectators;
    }
    
    @Override
    public boolean isPaused() {
        return paused;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public int getMaxRating() {
        return maxRating;
    }
    
    public SpleefMode getSpleefMode() {
        return spleefMode;
    }
    
    @Override
    public int getSize() {
        return spawns.length;
    }

    @Override
    public int getQueueLength() {
        if(getSpleefMode() == SpleefMode.NORMAL) {
            return SuperSpleef.getInstance().getBattleManagerSpleef().getGameQueue().getQueueLength(this);
        }
        else if(getSpleefMode() == SpleefMode.MULTI) {
            return SuperSpleef.getInstance().getBattleManagerMultiSpleef().getGameQueue().getQueueLength(this);
        }
        return -1;
    }

    @Override
    public int getQueuePosition(GeneralPlayer gp) {
        if(getSpleefMode() == SpleefMode.NORMAL) {
            return SuperSpleef.getInstance().getBattleManagerSpleef().getGameQueue().getQueuePosition(this, (SpleefPlayer)gp);
        }
        else if(getSpleefMode() == SpleefMode.MULTI) {
            return SuperSpleef.getInstance().getBattleManagerMultiSpleef().getGameQueue().getQueuePosition(this, (SpleefPlayer)gp);
        }
        return -1;
    }

    @Override
    public String getCurrentState() {
        if(occupied) {
            Battle battle = null;
            if(getSpleefMode() == SpleefMode.NORMAL) {
                battle = SuperSpleef.getInstance().getBattleManagerSpleef().getBattle(this);
            }
            else if(getSpleefMode() == SpleefMode.MULTI) {
                battle = SuperSpleef.getInstance().getBattleManagerMultiSpleef().getBattle(this);
            }
            if(getSpleefMode() == SpleefMode.NORMAL) {
                return ChatColor.GOLD + battle.getActivePlayers().get(0).getName() + ChatColor.GRAY + ChatColor.ITALIC + " vs. " + ChatColor.RESET + ChatColor.GOLD + battle.getActivePlayers().get(1).getName();
            }
            else {
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GRAY).append("Currently playing: ");
                for(SpleefPlayer sp : battle.getActivePlayers()) {
                    if(sb.length() > 0) {
                        sb.append(ChatColor.GRAY).append(", ");
                    }
                    sb.append(ChatColor.GOLD).append(sp.getName());
                }
                return sb.toString();
            }
        }
        else {
            return ChatColor.BLUE + "Empty";
        }
    }

    @Override
    public boolean isAvailable(GeneralPlayer gp) {
        return SuperSpleef.getInstance().getPlayerManager().get(gp.getPlayer()).getVisitedArenas().contains(this);
    }
    
    private static Map<String, Arena> arenas;
    
    public static Arena byName(String name) {
        Arena arena = arenas.get(name);
        if(arena == null) {
            for(Arena a : arenas.values()) {
                if(a.getName().equalsIgnoreCase(name)) {
                    arena = a;
                }
            }
        }
        return arena;
    }
    
    public static Collection<Arena> getAll() {
        return arenas.values();
    }
    
    public Battle startBattle(List<SpleefPlayer> players) {
        if(!isOccupied()) { //Shouldn't be necessary
            Battle battle = new Battle(this, players);
            battle.start();
            return battle;
        }
        return null;
    }
    
    public static void initialize(){
        arenas = new HashMap<>();
        MongoCursor<Document> dbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find().iterator();
        while(dbc.hasNext()) {
            Arena arena = EntityBuilder.load(dbc.next(), Arena.class);
            arenas.put(arena.getName(), arena);
        }
        SuperSpleef.getInstance().log("Loaded " + arenas.size() + " arenas!");
    }
    
    @Override
    public boolean isInGeneral() {
        return queued;
    }
}
