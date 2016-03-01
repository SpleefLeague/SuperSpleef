/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.mongodb.client.MongoCursor;
import com.spleefleague.core.events.BattleStartEvent;
import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 *
 * @author Jonas
 */
public class TeamSpleefArena extends Arena {
    
    @DBLoad(fieldName = "teamSizes")
    private int[] teamSizes;
    
    public int[] getTeamSizes() {
        return teamSizes;
    }
    
    @Override
    public TeamSpleefBattle startBattle(List<SpleefPlayer> players, BattleStartEvent.StartReason reason) {
        if(!isOccupied()) { //Shouldn't be necessary
            TeamSpleefBattle battle = new TeamSpleefBattle(this, players);
            battle.start(reason);
            return battle;
        }
        return null;
    }
    
    private static Map<String, TeamSpleefArena> arenas;
    
    public static TeamSpleefArena byName(String name) {
        TeamSpleefArena arena = arenas.get(name);
        if(arena == null) {
            for(TeamSpleefArena a : arenas.values()) {
                if(a.getName().equalsIgnoreCase(name)) {
                    arena = a;
                }
            }
        }
        return arena;
    }
    
    public static Collection<TeamSpleefArena> getAll() {
        return arenas.values();
    }
    
    public static void init(){
        arenas = new HashMap<>();
        MongoCursor<Document> dbc = SuperSpleef.getInstance().getPluginDB().getCollection("Arenas").find(new Document("spleefMode", "TEAM")).iterator();
        while(dbc.hasNext()) {
            TeamSpleefArena arena = EntityBuilder.load(dbc.next(), TeamSpleefArena.class);
            arenas.put(arena.getName(), arena);
            SuperSpleef.getInstance().getBattleManager().registerArena(arena);
        }
        SuperSpleef.getInstance().log("Loaded " + arenas.size() + " team spleef arenas!");
    }
}
