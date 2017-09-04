/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.scoreboards;

import com.spleefleague.core.io.typeconverters.LocationConverter;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import java.util.HashMap;
import java.util.List;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 *
 * @author Jonas
 */
public class Scoreboard extends DBEntity implements DBLoadable {

    private Integer[] orientation;
    private Vector offsetVector;
    @DBLoad(fieldName = "location", typeConverter = LocationConverter.class)
    private Location corner; //upper left
    private ScoreboardDefinition definition;
    private int[] scores;

    @DBLoad(fieldName = "definition", priority = 10)
    private void loadDefinition(ObjectId _id) {
        definition = ScoreboardDefinition.getDefinition(_id);
    }

    @DBLoad(fieldName = "orientation", priority = 0)
    private void loadOrientation(int orientation) {
        this.orientation = orientations.get(orientation);
        offsetVector = definition.getOffsetVector();
        switch (orientation) {
            case 1: {
                offsetVector = new Vector(offsetVector.getZ(), offsetVector.getY(), offsetVector.getX());
                break;
            }
            case 2: {
                offsetVector.multiply(new Vector(-1, 1, -1));
                break;
            }
            case 3: {
                offsetVector = new Vector(offsetVector.getZ(), offsetVector.getY(), offsetVector.getX()).multiply(new Vector(-1, 1, -1));
            }
        }
    }

    public void setScore(int... scores) {
        this.scores = scores;
        update();
    }

    public void update() {
        final int xinc = orientation[0], zinc = orientation[1];
        Location corner = this.corner.clone();
        for (int score : scores) {
            if (score > definition.getMaxScore()) {
                System.out.println("Error setting score of " + score + " at scoreboard with definition " + definition.getObjectId());
            } else {
                ScoreDefinition sdef = definition.getScoreDefinition(score);
                Location current = corner.clone();
                for (List<BlockData> templ : sdef.getBlockData()) {
                    for (BlockData bdata : templ) {
                        bdata.setBlockAt(current);
                        current.add(xinc, 0, zinc);
                    }
                    current.add(xinc * -templ.size(), -1, zinc * -templ.size());
                }
                corner.add(offsetVector);
            }
        }
    }

    private static final HashMap<Integer, Integer[]> orientations;

    static {
        ScoreboardDefinition.init();
        orientations = new HashMap<>();
        orientations.put(0, new Integer[]{1, 0});
        orientations.put(1, new Integer[]{0, 1});
        orientations.put(2, new Integer[]{-1, 0});
        orientations.put(3, new Integer[]{0, -1});
    }
}
