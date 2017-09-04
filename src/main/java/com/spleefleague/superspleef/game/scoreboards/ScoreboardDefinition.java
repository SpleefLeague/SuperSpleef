/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.scoreboards;

import com.mongodb.client.MongoCursor;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.DBSaveable;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.entitybuilder.TypeConverter;
import com.spleefleague.superspleef.SuperSpleef;
import java.util.HashMap;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.util.Vector;

/**
 *
 * @author Jonas
 */
public class ScoreboardDefinition extends DBEntity implements DBLoadable, DBSaveable {

    private HashMap<Integer, ScoreDefinition> definitionMap;
    private int maxScore = 0;
    @DBLoad(fieldName = "xOffset")
    private int xOffset;
    @DBLoad(fieldName = "yOffset")
    private int yOffset;
    @DBLoad(fieldName = "zOffset")
    private int zOffset;

    public Vector getOffsetVector() {
        return new Vector(xOffset, yOffset, zOffset);
    }

    public int getMaxScore() {
        return maxScore;
    }

    public ScoreDefinition getScoreDefinition(int score) {
        return definitionMap.get(score);
    }

    @DBLoad(fieldName = "definitions", typeConverter = ScoreDefinitionMapConverter.class)
    private void loadHashMap(HashMap<Integer, ScoreDefinition> definitions) {
        this.definitionMap = definitions;
        for (Integer score : definitions.keySet()) {
            maxScore = Math.max(maxScore, score);
        }
    }

    public static class ScoreDefinitionMapConverter extends TypeConverter<List, HashMap<Integer, ScoreDefinition>> {

        @Override
        public HashMap<Integer, ScoreDefinition> convertLoad(List t) {
            HashMap<Integer, ScoreDefinition> map = new HashMap<>();
            for (Object o : t) {
                Document doc = (Document) o;
                map.put(doc.get("score", Integer.class), EntityBuilder.load(doc.get("definition", Document.class), ScoreDefinition.class));
            }
            return map;
        }

        @Override
        public List convertSave(HashMap<Integer, ScoreDefinition> v) {
//            List list = new ArrayList<>();
//            for (Integer score : v.keySet()) {
//                Document doc = new Document();
//                doc.put("score", score);
//                doc.put("definition", EntityBuilder.serialize(v.get(score)));
//            }
//            return list;
            return null;
        }
    }

    private static HashMap<ObjectId, ScoreboardDefinition> definitions;

    public static void init() {
        definitions = new HashMap<>();
        MongoCursor<Document> dbc = SuperSpleef.getInstance().getPluginDB().getCollection("ScoreboardDefinitions").find().iterator();
        while (dbc.hasNext()) {
            ScoreboardDefinition definition = EntityBuilder.load(dbc.next(), ScoreboardDefinition.class);
            definitions.put(definition.getObjectId(), definition);
        }
    }

    public static ScoreboardDefinition getDefinition(ObjectId _id) {
        return definitions.get(_id);
    }
}
