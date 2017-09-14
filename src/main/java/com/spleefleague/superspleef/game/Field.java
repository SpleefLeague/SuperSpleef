package com.spleefleague.superspleef.game;

import com.mongodb.client.MongoCursor;
import com.spleefleague.core.utils.Area;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import com.spleefleague.entitybuilder.EntityBuilder;
import com.spleefleague.superspleef.SuperSpleef;
import com.spleefleague.virtualworld.FakeWorldManager;
import com.spleefleague.virtualworld.VirtualWorld;
import com.spleefleague.virtualworld.api.FakeBlock;
import com.spleefleague.virtualworld.api.FakeWorld;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 *
 * @author balsfull
 */
public class Field extends DBEntity implements DBLoadable {

    @DBLoad(fieldName = "field")
    private Area[] areas;
    private FakeWorld defaultWorld;
    @DBLoad(fieldName = "defaultVisible")
    private boolean defaultVisible = true;
    private Location low, high;
    
    /**
     * @return the field
     */
    public Area[] getAreas() {
        return areas;
    }

    public FakeWorld getDefaultWorld() {
        return defaultWorld;
    }

    public Location getLow() {
        return low;
    }

    public Location getHigh() {
        return high;
    }
    
    @Override
    public void done() {
        double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int i = 0; i < areas.length; i++) {
            minX = Math.min(minX, areas[i].getLow().getX());
            minY = Math.min(minY, areas[i].getLow().getY());
            minZ = Math.min(minZ, areas[i].getLow().getZ());
            maxX = Math.max(maxX, areas[i].getHigh().getX());
            maxY = Math.max(maxY, areas[i].getHigh().getY());
            maxZ = Math.max(maxZ, areas[i].getHigh().getZ());
        }
        this.high = new Location(areas[0].getHigh().getWorld(), maxX, maxY, maxZ);
        this.low = new Location(areas[0].getLow().getWorld(), minX, minY, minZ);
        if(!defaultVisible) {
            return;
        }
        com.spleefleague.virtualworld.Area vArea = new com.spleefleague.virtualworld.Area(high.toVector(), low.toVector());
        FakeWorldManager fwm = VirtualWorld.getInstance().getFakeWorldManager();
        this.defaultWorld = fwm.createWorld(high.getWorld(), vArea);
        for(Area area : this.areas) {
            for (Block block : area.getBlocks()) {
                FakeBlock fb = defaultWorld.getBlockAt(block.getLocation());
                fb.setType(Material.SNOW_BLOCK);
            }
        }
    }
    
    private static final Map<ObjectId, Field> FIELDS = new HashMap<>();
    
    public static Field getField(ObjectId id) {
        return FIELDS.get(id);
    }
    
    public static Collection<Field> getDefaultFields() {
        return FIELDS
                .values()
                .stream()
                .filter(f -> f.defaultVisible)
                .collect(Collectors.toSet());
    }
    
    public static void init() {
        MongoCursor<Document> cursor = SuperSpleef.getInstance().getPluginDB().getCollection("Fields").find().iterator();
        while (cursor.hasNext()) {
            Document d = cursor.next();
            try {
                Field field = EntityBuilder.load(d, Field.class);
                FIELDS.put(field.getObjectId(), field);
            } catch(Exception e) {
                SuperSpleef.getInstance().log("Error loading field " + d.get("_id"));
            }
        }
    }
}
