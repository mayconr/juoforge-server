package com.github.mayconr.juoserver.game.item.decay;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.item.WorldItem;

/** The drop date uses the world's persisted active-time clock, not wall-clock downtime. */
public final class ItemDecayState {
    public static final String DROPPED_AT = "item.droppedAt";

    private ItemDecayState() {
    }

    public static void recordDrop(WorldItem items, UOItem item, long now) {
        items.setProperty(item, DROPPED_AT, now, false);
    }

    public static long droppedAt(WorldItem items, UOItem item, long now) {
        Number date = items.computeProperty(item, DROPPED_AT,
                (key, value) -> value instanceof Number number ? number : now, false);
        return date.longValue();
    }

    public static boolean isDue(WorldItem items, UOItem item, long now, double durationSeconds) {
        return now - droppedAt(items, item, now) >= durationSeconds * 1000.0;
    }
}
