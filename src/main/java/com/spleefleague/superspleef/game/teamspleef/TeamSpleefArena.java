/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.teamspleef;

import com.spleefleague.core.events.BattleStartEvent.StartReason;
import com.spleefleague.entitybuilder.DBLoad;
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
 * @author Jonas
 */
public class TeamSpleefArena extends Arena<TeamSpleefBattle> {

    @DBLoad(fieldName = "teamSizes")
    private int[] teamSizes;

    public int[] getTeamSizes() {
        return teamSizes;
    }

    @Override
    public TeamSpleefBattle startBattle(List<SpleefPlayer> players, StartReason reason) {
        if (!isOccupied()) { //Shouldn't be necessary
            TeamSpleefBattle battle = new TeamSpleefBattle(this, players);
            battle.start(reason);
            return battle;
        }
        return null;
    }
    
    private static final Map<String, TeamSpleefArena> arenas = new HashMap<>();
    
    public static Collection<TeamSpleefArena> getAll() {
        return arenas.values();
    }
    
    public static TeamSpleefArena byName(String arena) {
        return arenas.get(arena.toLowerCase());
    }
    
    public static void loadArena(Document document) {
        TeamSpleefArena arena = EntityBuilder.load(document, TeamSpleefArena.class);
        if(arenas.containsKey(arena.getName().toLowerCase())) {
            Arena.recursiveCopy(arena, byName(arena.getName()), Arena.class);
        }
        else {
            SuperSpleef.getInstance().getTeamSpleefBattleManager().registerArena(arena);
            arenas.put(arena.getName().toLowerCase(), arena);
        }
    }
}