/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.spleef;

import com.spleefleague.core.events.BattleStartEvent;
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
public class NormalSpleefArena extends Arena<NormalSpleefBattle> {

    @Override
    public NormalSpleefBattle startBattle(List<SpleefPlayer> players, BattleStartEvent.StartReason reason) {
        if (!isOccupied()) { //Shouldn't be necessary
            NormalSpleefBattle battle = new NormalSpleefBattle(this, players);
            battle.start(reason);
            return battle;
        }
        return null;
    }
    
    private static final Map<String, NormalSpleefArena> arenas = new HashMap<>();
    
    public static Collection<NormalSpleefArena> getAll() {
        return arenas.values();
    }
    
    public static NormalSpleefArena byName(String arena) {
        return arenas.get(arena.toLowerCase());
    }
    
    public static void loadArena(Document document) {
        NormalSpleefArena arena = EntityBuilder.load(document, NormalSpleefArena.class);
        if(arenas.containsKey(arena.getName().toLowerCase())) {
            Arena.recursiveCopy(arena, byName(arena.getName()), Arena.class);
        }
        else {
            SuperSpleef.getInstance().getNormalSpleefBattleManager().registerArena(arena);
            arenas.put(arena.getName().toLowerCase(), arena);
        }
    }
}
