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
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.queue.QueueableArena;
import com.spleefleague.core.utils.Area;
import com.spleefleague.core.utils.function.Dynamic;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.scoreboards.Scoreboard;
import com.spleefleague.superspleef.player.SpleefPlayer;
import com.sun.xml.internal.ws.api.server.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    @DBLoad(fieldName = "scoreboards")
    private Scoreboard[] scoreboards;
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
    
    public Scoreboard[] getScoreboards() {
        return scoreboards;
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
    public int getQueuePosition(UUID uuid) {
        if(getSpleefMode() == SpleefMode.NORMAL) {
            return SuperSpleef.getInstance().getBattleManagerSpleef().getGameQueue().getQueuePosition(this, uuid);
        }
        else if(getSpleefMode() == SpleefMode.MULTI) {
            return SuperSpleef.getInstance().getBattleManagerMultiSpleef().getGameQueue().getQueuePosition(this, uuid);
        }
        return -1;
    }
    
    public Dynamic<List<String>> getDynamicDescription() {
        return (SLPlayer slp) -> {
            List<String> description = new ArrayList<>();
            SpleefPlayer sp = SuperSpleef.getInstance().getPlayerManager().get(slp.getUniqueId());
            if(Arena.this.isAvailable(sp.getUniqueId())) {
                if(Arena.this.isPaused()) {
                    description.add(ChatColor.RED + "This arena is");
                    description.add(ChatColor.RED + "currently paused.");
                }
                else if(Arena.this.isOccupied()) {
                    Battle battle;
                    if(getSpleefMode() == SpleefMode.NORMAL) {
                        battle = SuperSpleef.getInstance().getBattleManagerSpleef().getBattle(Arena.this);
                        description.add(ChatColor.GOLD + battle.getActivePlayers().get(0).getName() + ChatColor.GRAY + ChatColor.ITALIC + " vs. " + ChatColor.RESET + ChatColor.GOLD + battle.getActivePlayers().get(1).getName());
                        description.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Click to spectate");
                    }
                    else if(getSpleefMode() == SpleefMode.MULTI) {
                        description.add(ChatColor.GRAY + "Currently playing: ");
                        battle = SuperSpleef.getInstance().getBattleManagerMultiSpleef().getBattle(Arena.this);
                        battle.getActivePlayers().stream().forEach((player) -> {
                            description.add(ChatColor.GOLD + player.getName());
                        });
                        description.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Click to spectate");
                    }
                    else {
                        description.add("PLACEHOLDER");
                    }
                }
                else {
                    description.add(ChatColor.GREEN + "" + Arena.this.getQueueLength() + ChatColor.GRAY + "/" + ChatColor.GREEN + Arena.this.getSize());
                    description.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Click to join the queue");
                }
            }
            else {
                description.add(ChatColor.RED + "You have not discovered");
                description.add(ChatColor.RED + "this arena yet.");
            }
            return description;
        };
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
    public boolean isAvailable(UUID uuid) {
        return SuperSpleef.getInstance().getPlayerManager().get(uuid).getVisitedArenas().contains(this);
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
    
    public static void init(){
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
