package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;

/** Changes to a ground item. Null fields preserve their current values; an empty change does nothing. */
public record ItemChange(Location location, Integer modelId) {
    public ItemChange {
        location = location == null ? null : new PointInTheWorld(location);
        if (modelId != null && (modelId < 0 || modelId > 0xFFFF)) {
            throw new IllegalArgumentException("modelId must fit in an unsigned short");
        }
    }

    public ItemChange() {
        this(null, null);
    }
}
