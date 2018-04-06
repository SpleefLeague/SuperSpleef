/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.power;

import com.spleefleague.gameapi.events.BattleStartEvent.StartReason;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.game.Arena;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author jonas
 */
public class PowerSpleefArena extends Arena<PowerSpleefBattle> {

    @Override
    public PowerSpleefBattle startBattle(List<SpleefPlayer> players, StartReason reason) {
        if (!isOccupied()) { //Shouldn't be necessary
            PowerSpleefBattle battle = new PowerSpleefBattle(this, players);
            battle.start(reason);
            return battle;
        }
        return null;
    }
    
    private static final Map<String, PowerSpleefArena> arenas = new HashMap<>();
    
    public static Collection<PowerSpleefArena> getAll() {
        return arenas.values();
    }
    
    public static PowerSpleefArena byName(String arena) {
        return arenas.get(arena.toLowerCase());
    }
    
    public static void loadArena(Document document) {
        PowerSpleefArena arena = EntityBuilder.load(document, PowerSpleefArena.class);
        if(arenas.containsKey(arena.getName().toLowerCase())) {
            Arena.recursiveCopy(arena, byName(arena.getName()), Arena.class);
        }
        else {
            SuperSpleef.getInstance().getPowerSpleefBattleManager().registerArena(arena);
            arenas.put(arena.getName().toLowerCase(), arena);
        }
    }
}
