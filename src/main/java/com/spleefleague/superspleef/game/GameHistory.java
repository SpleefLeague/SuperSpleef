/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.gameapi.events.BattleEndEvent.EndReason;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.superspleef.game.GameHistoryPlayerData.GameHistoryPlayerDataTypeConverter;
import com.spleefleague.superspleef.game.SpleefBattle.PlayerData;
import java.util.Date;
import com.spleefleague.superspleef.player.SpleefPlayer;

/**
 *
 * @author Jonas
 */
public class GameHistory extends DBEntity implements DBSaveable {

    @DBSave(fieldName = "players", typeConverter = GameHistoryPlayerDataTypeConverter.class)
    private final GameHistoryPlayerData[] players;
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
        players = new GameHistoryPlayerData[battle.getPlayers().size()];
        int i = 0;
        for (SpleefPlayer sp : battle.getPlayers()) {
            PlayerData playerData = battle.getData(sp);
            players[i++] = new GameHistoryPlayerData(sp, battle.getData(sp).getPoints(), sp == winner, playerData.getRemoveReason());
        }
        this.duration = battle.getDuration();
        startDate = new Date(System.currentTimeMillis() - this.duration * 50);
        this.arena = battle.getArena().getName();
        this.spleefMode = battle.getArena().getSpleefMode();
    }

    public GameHistoryPlayerData[] getPlayers() {
        return players;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getDuration() {
        return duration;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    public SpleefMode getSpleefMode() {
        return spleefMode;
    }

    public String getArena() {
        return arena;
    }
    
    
}
