package com.github.mayconr.juoserver.game.item.hold;

import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOItem;

/** The lifted item and its position before any lift or drop mutation. */
public record HeldItem(UOItem item, ItemLocation origin, PointInTheWorld position) {
    public static HeldItem capture(UOItem item) {
        return new HeldItem(item, item.getCurrentLocation(),
                new PointInTheWorld(item.getX(), item.getY(), item.getZ()));
    }
}
