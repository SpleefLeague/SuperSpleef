/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.core.io.typeconverters.LocationConverter;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.core.queue.QueueableArena;
import com.spleefleague.core.utils.Area;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.multi.MultiSpleefArena;
import com.spleefleague.superspleef.game.power.PowerSpleefArena;
import com.spleefleague.superspleef.game.scoreboards.Scoreboard;
import com.spleefleague.superspleef.game.classic.NormalSpleefArena;
import com.spleefleague.superspleef.game.team.TeamSpleefArena;
import com.spleefleague.superspleef.player.SpleefPlayer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 *
 * @author Jonas
 */
public abstract class Arena<B extends SpleefBattle> extends DBEntity implements DBLoadable, DBSaveable, QueueableArena<SpleefPlayer> {

    @DBLoad(fieldName = "border")
    private Area border;
    private Field field;
    private Location[] spawns;
    @DBLoad(fieldName = "requiredPlayers", priority = 0)
    private int requiredPlayers;
    @DBLoad(fieldName = "creator")
    private String creator;
    @DBLoad(fieldName = "name")
    private String name;
    @DBLoad(fieldName = "description")
    private List<String> description = Collections.emptyList();
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
    private int maxRating = -1;
    @DBLoad(fieldName = "area")
    private Area area;
    @DBLoad(fieldName = "spleefMode")
    private SpleefMode spleefMode = SpleefMode.CLASSIC;
    private int runningGames = 0;
    
    public Location[] getSpawns() {
        return spawns;
    }

    public Area getBorder() {
        return border;
    }

    @Override
    public List<String> getDescription() {
        return description;
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

    public Function<SLPlayer, List<String>> getDynamicDescription() {
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
            if(spleefMode != SpleefMode.MULTI) {
                maxRating = 5;
            }
        }
    }
    
    public abstract B startBattle(List<SpleefPlayer> player, StartReason reason);
    
    @Override
    public final boolean equals(Object o) {
        if(o instanceof QueueableArena) {
            QueueableArena other = (QueueableArena) o;
            return other.getName().equalsIgnoreCase(getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    public static Arena byName(String name, SpleefMode mode) {
        switch(mode) {
            case CLASSIC: {
                return NormalSpleefArena.byName(name);
            }
            case MULTI: {
                return MultiSpleefArena.byName(name);
            }
            case TEAM: {
                return TeamSpleefArena.byName(name);
            }
            case POWER: {
                return PowerSpleefArena.byName(name);
            }
        }
        return null;
    }

    public static Collection<? extends Arena<?>> getAll() {
        return Stream.of(
                NormalSpleefArena.getAll(),
                MultiSpleefArena.getAll(),
                TeamSpleefArena.getAll(),
                PowerSpleefArena.getAll()
        ).flatMap(c -> c.stream()).collect(Collectors.toList());  
    }
    
    protected static void recursiveCopy(Object src, Object target, Class targetClass) {
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
        Iterator<Document> arenaTypes = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").aggregate(Arrays.asList(
                new Document("$unwind", new Document("path", "$spleefMode")),
                new Document("$group", new Document("_id", "$spleefMode").append("arenas", new Document("$addToSet", "$$ROOT")))
        )).iterator();
        while(arenaTypes.hasNext()) {
            Document arenas = arenaTypes.next();
            List<Document> arenaInstances = arenas.get("arenas", List.class);
            try {
                SpleefMode mode = SpleefMode.valueOf(arenas.get("_id", String.class));
                int amount;
                switch(mode) {
                    case CLASSIC: {
                        amount = loadArenas(arenaInstances, NormalSpleefArena::loadArena);
                        break;
                    }
                    case MULTI: {
                        amount = loadArenas(arenaInstances, MultiSpleefArena::loadArena);
                        break;
                    }
                    case TEAM: {
                        amount = loadArenas(arenaInstances, TeamSpleefArena::loadArena);
                        break;
                    }
                    case POWER: {
                        amount = loadArenas(arenaInstances, PowerSpleefArena::loadArena);
                        break;
                    }
                    default: {
                        continue;
                    }
                }
                String modeName = mode.toString().substring(0, 1).toUpperCase().concat(mode.toString().substring(1).toLowerCase());
                SuperSpleef.getInstance().log("Loaded " + amount + " " + modeName + " Spleef arenas.");
            } catch(IllegalArgumentException e) {
                System.err.println(arenas.get("_id") + " is not a valid spleef mode.");
            }
        }
    }
    
    private static int loadArenas(List<Document> arenas, Consumer<Document> arenaCreator) {
        int successCounter = 0;
        for(Document arena : arenas) {
            try {
                arenaCreator.accept(arena);
                successCounter++;
            } catch(Exception e) {
                System.err.println("Error loading arena " + arena.get("name") + " (" + arena.get("type") + ")");
                e.printStackTrace();
            }
        }
        return successCounter;
    }
}
