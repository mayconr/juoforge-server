package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;

/**
 * Changes to an item. Location and model apply to ground items, and null fields preserve their current
 * values; an empty change does nothing. A flip alternates the model of a flippable item and cannot be
 * combined with the other changes.
 */
public record ItemChange(Location location, Integer modelId, boolean flip) {
    public ItemChange {
        location = location == null ? null : new PointInTheWorld(location);
        if (modelId != null && (modelId < 0 || modelId > 0xFFFF)) {
            throw new IllegalArgumentException("modelId must fit in an unsigned short");
        }
        if (flip && (location != null || modelId != null)) {
            throw new IllegalArgumentException("A flip cannot be combined with location or model changes");
        }
    }

    public ItemChange(Location location, Integer modelId) {
        this(location, modelId, false);
    }

    public ItemChange() {
        this(null, null);
    }

    /** Alternates the model of a flippable item between its template model and the next one. */
    public static ItemChange flipModel() {
        return new ItemChange(null, null, true);
    }

    public boolean isEmpty() {
        return location == null && modelId == null && !flip;
    }
}
