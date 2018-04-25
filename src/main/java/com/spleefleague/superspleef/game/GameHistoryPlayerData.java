/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game;

import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBSave;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.entitybuilder.TypeConverter;
import com.spleefleague.superspleef.player.SpleefPlayer;
import java.util.UUID;
import org.bson.Document;

/**
 *
 * @author jonas
 */
public class GameHistoryPlayerData extends DBEntity implements DBSaveable {

    private SpleefPlayer player;
    @DBSave(fieldName = "points")
    private int points;
    @DBSave(fieldName = "winner")
    private Boolean winner;
    @DBSave(fieldName = "surrendered")
    private Boolean surrendered;
    @DBSave(fieldName = "quit")
    private Boolean quit;
    private final Document metadata;

    public GameHistoryPlayerData(SpleefPlayer player, int points, boolean winner, RemoveReason removeReason) {
        metadata = new Document();
        this.player = player;
        this.points = points;
        this.winner = winner;
        if(removeReason == RemoveReason.SURRENDER) {
            surrendered = true;
        }
        else if(removeReason == RemoveReason.QUIT) {
            quit = true;
        }
    }
    
    public Document getMetadata() {
        return metadata;
    }

    @DBSave(fieldName = "uuid", typeConverter = TypeConverter.UUIDStringConverter.class)
    public UUID getUuid() {
        return player.getUniqueId();
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isWinner() {
        return winner == true;
    }

    public void setWinner(Boolean winner) {
        this.winner = winner;
    }

    public boolean hasSurrenderd() {
        return surrendered == true;
    }

    public void setSurrendered(Boolean surrendered) {
        this.surrendered = surrendered;
    }

    public boolean hasQuit() {
        return quit == null;
    }

    public SpleefPlayer getPlayer() {
        return player;
    }
    
    

    public void setQuit(Boolean quit) {
        this.quit = quit;
    }
    
    public static class GameHistoryPlayerDataTypeConverter extends TypeConverter<Document, GameHistoryPlayerData> {

        @Override
        public GameHistoryPlayerData convertLoad(Document t) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Document convertSave(GameHistoryPlayerData v) {
            Document doc = EntityBuilder.serialize(v).get("$set", Document.class);
            v.metadata.entrySet().forEach(e -> doc.put(e.getKey(), e.getValue()));
            return doc;
        }
    }
}
