package com.spleefleague.superspleef.game;

import com.spleefleague.core.utils.Area;
import com.spleefleague.entitybuilder.DBEntity;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBLoadable;
import java.util.UUID;

/**
 *
 * @author balsfull
 */
public class Field extends DBEntity implements DBLoadable {

    @DBLoad(fieldName = "uuid")
    private UUID uuid;
    @DBLoad(fieldName = "field")
    private Area[] field;

    /**
     * @return the uuid
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * @return the field
     */
    public Area[] getField() {
        return field;
    }
}
