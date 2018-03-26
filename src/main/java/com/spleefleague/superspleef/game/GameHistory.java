/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.core.events.BattleEndEvent.EndReason;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.TypeConverter;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import com.spleefleague.superspleef.player.SpleefPlayer;

/**
 *
 * @author Jonas
 */
public class GameHistory extends DBEntity implements DBSaveable {

    @DBSave(fieldName = "players")
    private final PlayerData[] players;
    @DBSave(fieldName = "date")
    private final Date startDate;
    @DBSave(fieldName = "duration")
    private final int duration; //In ticks
    @DBSave(fieldName = "endReason")
    private final EndReason endReason;
    @DBSave(fieldName = "spleefMode")
    private final SpleefMode spleefMode;
    @DBSave(fieldName = "arena")
    private final String arena;

    protected GameHistory(SpleefBattle<?> battle, SpleefPlayer winner, EndReason endReason) {
        this.endReason = endReason;
        players = new PlayerData[battle.getPlayers().size()];
        Collection<SpleefPlayer> activePlayers = battle.getActivePlayers();
        int i = 0;
        for (SpleefPlayer sp : battle.getPlayers()) {
            players[i++] = new PlayerData(sp.getUniqueId(), battle.getData(sp).getPoints(), sp == winner, !activePlayers.contains(sp));
        }
        this.duration = battle.getDuration();
        startDate = new Date(System.currentTimeMillis() - this.duration * 50);
        this.arena = battle.getArena().getName();
        this.spleefMode = battle.getArena().getSpleefMode();
    }

    public static class PlayerData extends DBEntity implements DBSaveable {

        @DBSave(fieldName = "uuid", typeConverter = TypeConverter.UUIDStringConverter.class)
        private final UUID uuid;
        @DBSave(fieldName = "points")
        private final int points;
        @DBSave(fieldName = "winner")
        private final Boolean winner;
        @DBSave(fieldName = "surrendered")
        private final Boolean surrendered;

        public PlayerData(UUID uuid, int points, boolean winner, boolean surrendered) {
            this.uuid = uuid;
            this.points = points;
            this.winner = winner;
            this.surrendered = surrendered;
        }
    }
}
