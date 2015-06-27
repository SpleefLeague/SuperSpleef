/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spleefleague.superspleef.game.signs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.spleefleague.core.io.DBEntity;
import net.spleefleague.core.io.DBLoad;
import net.spleefleague.core.io.DBLoadable;
import net.spleefleague.core.io.EntityBuilder;
import net.spleefleague.core.io.TypeConverter;
import net.spleefleague.superspleef.SuperSpleef;
import net.spleefleague.superspleef.game.Arena;
import net.spleefleague.superspleef.game.Battle;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.block.Sign;

/**
 *
 * @author Jonas
 */
public class GameSign extends DBEntity implements  DBLoadable{
    
    private Location location;
    private Arena arena;
    
    public GameSign() {
        
    }
    
    @DBLoad(fieldName = "location", typeConverter = TypeConverter.LocationConverter.class)
    private void setSign(Location location) {
        this.location = location;
    }
    
    @DBLoad(fieldName = "arena")
    private void setArena(String name) {
        arena = Arena.byName(name);
        if(signs.get(arena) == null) {
            signs.put(arena, new HashSet<GameSign>());
        }
        signs.get(arena).add(this);
    }
    
    public Sign getSign() {
        return (Sign)location.getBlock().getState();
    }
    
    public Location getLocation() {
        return location;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public Battle getBattle() {
        return SuperSpleef.getInstance().getBattleManager().getBattle(arena);
    }
    
    public void updateStatus() {
        Sign sign = getSign();
        if (sign != null) {
            sign.setLine(0, "[" + (arena.isPaused() ? ChatColor.DARK_RED + "Closed" : (arena.isOccupied() ? ChatColor.YELLOW + "Occupied" : ChatColor.GREEN + "Free")) + ChatColor.RESET + "]");
            sign.setLine(1, getArena().getName());
            if (!arena.isPaused()) {
                if (!arena.isOccupied()) {
                    int required = (arena.getSize() - arena.getQueueLength());
                    sign.setLine(2, "Waiting for " + required);
                    sign.setLine(3, "more player" + (required == 1 ? "" : "s"));
                } else {
                    int waiting = arena.getQueueLength();
                    sign.setLine(2, waiting + " player" + (waiting == 1 ? "" : "s"));
                    sign.setLine(3, "currently waiting");
                }
            }
            sign.update();
        }
    }
    
    private static Map<Arena, HashSet<GameSign>> signs;

    public static void initialize() {
        signs = new HashMap<>();
        for(Document document : SuperSpleef.getInstance().getPluginDB().getCollection("GameSigns").find()) {
            EntityBuilder.load(document, GameSign.class).updateStatus();
        }
    }
    
    public static HashSet<GameSign> getGameSigns(Arena arena) {
        return signs.get(arena);
    }
    
    public static void updateGameSigns(Arena arena) {
        for(GameSign gs : getGameSigns(arena)) {
            gs.updateStatus();
        }
    }
    
    public static void updateGameSigns() {
        for(Arena arena : signs.keySet()) {
            updateGameSigns(arena);
        }
    }
    
    public static HashSet<GameSign> getAll() {
        HashSet<GameSign> all = new HashSet<>();
        for(HashSet<GameSign> gs : signs.values()) {
            all.addAll(gs);
        }
        return all;
    }
}