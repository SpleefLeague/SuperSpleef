/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.mongodb.client.MongoCursor;
import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.io.typeconverters.LocationConverter;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.queue.QueueableArena;
import com.spleefleague.core.utils.Area;
import com.spleefleague.core.utils.function.Dynamic;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.powerspleef.PowerSpleefBattle;
import com.spleefleague.superspleef.game.scoreboards.Scoreboard;
import com.spleefleague.superspleef.player.SpleefPlayer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 *
 * @author Jonas
 */
public class Arena extends DBEntity implements DBLoadable, DBSaveable, QueueableArena<SpleefPlayer> {

    @DBLoad(fieldName = "border")
    private Area border;
    private Field field;
    private Location[] spawns;
    @DBLoad(fieldName = "requiredPlayers", priority = 2)
    private int requiredPlayers;
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
    @DBLoad(fieldName = "spectatorSpawn", typeConverter = LocationConverter.class)
    private Location spectatorSpawn; //null -> default world spawn
    @DBLoad(fieldName = "isDefault")
    private boolean defaultArena = false;
    @DBLoad(fieldName = "maxRating")
    private int maxRating = 5;
    @DBLoad(fieldName = "area")
    private Area area;
    @DBLoad(fieldName = "spleefMode")
    private SpleefMode spleefMode = SpleefMode.NORMAL;
    private int runningGames = 0;

    public Location[] getSpawns() {
        return spawns;
    }

    public Area getBorder() {
        return border;
    }
    
    @DBLoad(fieldName = "spawns", typeConverter = LocationConverter.class, priority = 1)
    private void setSpawns(Location[] spawns) {
        this.spawns = spawns;
        this.requiredPlayers = spawns.length;//Will be overwritten if requiredPlayers value exists
    }
    
    @DBLoad(fieldName = "field")
    public void setField(ObjectId id) {
        this.field = Field.getField(id);
    }
    
    public Field getField() {
        return field;
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
        return false;
    }

    public int getRunningGamesCount() {
        return runningGames;
    }

    public void registerGameStart() {
        runningGames++;
    }

    public void registerGameEnd() {
        runningGames--;
    }

    public Area getArea() {
        return area;
    }

    public boolean isRated() {
        return rated;
    }

    @Deprecated
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
    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    @Override
    public int getSize() {
        return spawns.length;
    }

    public Dynamic<List<String>> getDynamicDescription() {
        return (SLPlayer slp) -> {
            List<String> description = new ArrayList<>();
            SpleefPlayer sjp = SuperSpleef.getInstance().getPlayerManager().get(slp.getUniqueId());
            if (Arena.this.isAvailable(sjp)) {
                if (Arena.this.isPaused()) {
                    description.add(ChatColor.RED + "This arena is");
                    description.add(ChatColor.RED + "currently paused.");
                } else if (getRunningGamesCount() == 0) {
                    description.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Click to join the queue");
                }
            } else {
                description.add(ChatColor.RED + "You have not discovered");
                description.add(ChatColor.RED + "this arena yet.");
            }
            return description;
        };
    }

    @Override
    public boolean isAvailable(SpleefPlayer sp) {
        return this.isDefaultArena() || sp.getVisitedArenas().contains(this);
    }

    @Override
    public boolean isQueued() {
        return queued;
    }
    
    public boolean isDefaultArena() {
        return this.defaultArena;
    }
    
    @Override
    public void done() {
        if(maxRating == -1) {
            if(spleefMode == SpleefMode.NORMAL) {
                maxRating = 5;
            }
        }
    }

    public SpleefBattle startBattle(List<SpleefPlayer> players, StartReason reason) {
        if (!isOccupied()) { //Shouldn't be necessary
            SpleefBattle battle;
            if(this.getSpleefMode() == SpleefMode.POWER) {
                battle = new PowerSpleefBattle(this, players);
            }
            else {
                battle = new NormalSpleefBattle(this, players);
            }
            battle.start(reason);
            return battle;
        }
        return null;
    }
    
    @Override
    public final boolean equals(Object o) {
        if(o instanceof QueueableArena) {
            QueueableArena other = (QueueableArena) o;
            return other.getName().equalsIgnoreCase(getName());
        }
        return false;
    }

    private static Map<String, Arena> arenas;

    public static Arena byName(String name) {
        Arena arena = arenas.get(name);
        if (arena == null) {
            for (Arena a : arenas.values()) {
                if (a.getName().equalsIgnoreCase(name)) {
                    arena = a;
                }
            }
        }
        return arena;
    }

    public static Collection<? extends Arena> getAll() {
        return arenas.values();
    }
    
    /**
     * 
     * @param name Name of the arena to reload
     * @return true if the arena exists
     */
    public static boolean reload(String name) {
        Document d = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("name", name)).first();
        if(d == null) {
            return false;
        }
        else {
            Arena arena = loadArena(d);
            if (arena == null) return false;
            Arena old = Arena.byName(name);
            if(old != null) {
                recursiveCopy(arena, old, Arena.class);
            }
            else {
                if(arena.getSpleefMode() == SpleefMode.NORMAL) {
                    addArena(arena, true);
                }
                else if(arena.getSpleefMode() == SpleefMode.MULTI) {
                    addArena(arena, false);
                }
                else {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static void addArena(Arena arena, boolean classic) {
        arenas.put(arena.getName(), arena);
        if (classic) {
            SuperSpleef.getInstance().getNormalSpleefBattleManager().registerArena(arena);
        }
        else {
            SuperSpleef.getInstance().getMultiSpleefBattleManager().registerArena(arena);
        }
    }
    
    private static void recursiveCopy(Object src, Object target, Class targetClass) {
        Class srcClass = src.getClass();
        while(true) {
            for(java.lang.reflect.Field f : srcClass.getDeclaredFields()) {
                try {
                    f.setAccessible(true);
                    f.set(target, f.get(src));
                } catch (Exception ex) {
                    Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(srcClass == Object.class || srcClass == targetClass) {
                break;
            }
            srcClass = srcClass.getSuperclass();
            if(srcClass == null) {
                break;
            }
        }
    }

    public static void init() {
        arenas = new HashMap<>();
        MongoCursor<Document> normalDbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("spleefMode", "NORMAL")).iterator();
        while (normalDbc.hasNext()) {
            Document d = normalDbc.next();
            try {
                Arena arena = loadArena(d);
                if (arena == null) continue;
                addArena(arena, true);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading " + d.get("name"));
            }
        }
        MongoCursor<Document> multiDbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("spleefMode", "MULTI")).iterator();
        while (multiDbc.hasNext()) {
            Document d = multiDbc.next();
            try {
                Arena arena = loadArena(d);
                if (arena == null) continue;
                addArena(arena, false);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading " + d.get("name"));
            }
        }
        SuperSpleef.getInstance().log("Loaded " + arenas.size() + " arenas!");
    }
    
    private static Arena loadArena(Document doc) {
        return EntityBuilder.load(doc, Arena.class);
    }
}
