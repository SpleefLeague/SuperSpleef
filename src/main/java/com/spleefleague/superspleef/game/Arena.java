/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.mongodb.client.MongoCursor;
import com.spleefleague.core.SpleefLeague;
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
import com.spleefleague.fakeblocks.representations.FakeArea;
import com.spleefleague.fakeblocks.representations.FakeBlock;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.scoreboards.Scoreboard;
import com.spleefleague.superspleef.player.SpleefPlayer;

import java.util.*;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Jonas
 */
public class Arena extends DBEntity implements DBLoadable, DBSaveable, QueueableArena<SpleefPlayer> {

    @DBLoad(fieldName = "border")
    private Area border;
    private Area[] fields;
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
    private ObjectId sharedField;

    private int runningGames = 0;
    private FakeArea defaultSnow;

    public Location[] getSpawns() {
        return spawns;
    }

    public Area getBorder() {
        return border;
    }

    public FakeArea getDefaultSnow() {
        return defaultSnow;
    }
    
    @DBLoad(fieldName = "spawns", typeConverter = LocationConverter.class, priority = 1)
    private void setSpawns(Location[] spawns) {
        this.spawns = spawns;
        this.requiredPlayers = spawns.length;//Will be overwritten if requiredPlayers value exists
    }

    @DBLoad(fieldName = "field")
    public void setField(Area[] field) {
        this.fields = field;
        if (sharedField != null) {
            defaultSnow = new FakeArea();
            for (Arena arena : Arena.getAll()) {
                if (arena.getObjectId().equals(sharedField)) {
                    defaultSnow = arena.getDefaultSnow();
                    break;
                }
            }
        }
        if (defaultSnow == null) {
            defaultSnow = new FakeArea();
            for (Area f : fields) {
                for (Block block : f.getBlocks()) {
                    defaultSnow.addBlock(new FakeBlock(block.getLocation(), Material.SNOW_BLOCK));
                }
            }
        }
        SpleefLeague.getInstance().getFakeBlockHandler().addArea(defaultSnow, false, Bukkit.getOnlinePlayers().toArray(new Player[0]));
    }

    @DBLoad(fieldName = "sharedField")
    public void setSharedField(ObjectId sharedField) {
        for (Arena arena : Arena.getAll()) {
            if (arena.getObjectId().equals(sharedField)) {
                defaultSnow = arena.getDefaultSnow();
                fields = arena.getField();
                break;
            }
        }
    }

    public Area[] getField() {
        return fields;
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
            SpleefBattle battle = new NormalSpleefBattle(this, players);
            battle.start(reason);
            return battle;
        }
        return null;
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

    public static void init() {
        arenas = new HashMap<>();
        MongoCursor<Document> normalDbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("spleefMode", "NORMAL")).iterator();
        while (normalDbc.hasNext()) {
            Document doc = normalDbc.next();
            try {
                Arena arena = EntityBuilder.load(doc, Arena.class);
                arenas.put(arena.getName(), arena);
                SuperSpleef.getInstance().getNormalSpleefBattleManager().registerArena(arena);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading arena: " + doc.getString("name"));
            }
        }
        MongoCursor<Document> multiDbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("spleefMode", "MULTI")).iterator();
        while (multiDbc.hasNext()) {
            Document doc = multiDbc.next();
            try {
               Arena arena = EntityBuilder.load(doc, Arena.class);
                arenas.put(arena.getName(), arena);
                SuperSpleef.getInstance().getMultiSpleefBattleManager().registerArena(arena);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading arena: " + doc.getString("name"));
            }
        }
        SuperSpleef.getInstance().log("Loaded " + arenas.size() + " arenas!");
    }

    @Override
    public final boolean equals(Object o) {
        if(o instanceof QueueableArena) {
            QueueableArena other = (QueueableArena) o;
            return other.getName().equalsIgnoreCase(getName());
        }
        return false;
    }
}
