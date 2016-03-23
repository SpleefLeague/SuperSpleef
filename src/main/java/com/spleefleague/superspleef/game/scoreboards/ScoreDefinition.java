/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.superspleef.game.scoreboards;

import com.spleefleague.core.io.DBEntity;
import com.spleefleague.core.io.DBLoad;
import com.spleefleague.core.io.DBLoadable;
import com.spleefleague.core.io.EntityBuilder;
import com.spleefleague.core.io.TypeConverter;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author Jonas
 */
public class ScoreDefinition extends DBEntity implements DBLoadable {

    @DBLoad(fieldName = "blockdata")
    private List<List<BlockData>> blockdata;

    public List<List<BlockData>> getBlockData() {
        return blockdata;
    }

    public static class BlockDataListConverter extends TypeConverter<List, List<List<BlockData>>> {

        @Override
        public List<List<BlockData>> convertLoad(List t) {
            List<List<BlockData>> list1 = new ArrayList<>();
            for (Object o1 : t) {
                List l = (List) o1;
                List<BlockData> list2 = new ArrayList<>();
                for (Object o2 : l) {
                    list2.add(EntityBuilder.deserialize((Document) o2, BlockData.class));
                }
                list1.add(list2);
            }
            return list1;
        }

        @Override
        public List convertSave(List<List<BlockData>> v) {
            return null;
        }
    }
}
