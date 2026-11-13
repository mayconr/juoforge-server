package com.github.mayconr.juoserver.infrastructure.datafile;

import java.util.Set;

/**
 * Client tiledata and art dimensions for an item graphic, decoupled from the file reader library.
 *
 * @param modelId    item graphic id
 * @param name       tiledata name; stackable names usually carry the {@code %s} plural suffix
 * @param stackable  whether the client treats the graphic as a stack (tiledata {@code GENERIC} flag)
 * @param flags      names of every tiledata flag set for the graphic
 * @param weight     tiledata weight
 * @param layer      tiledata equipment layer
 * @param count      tiledata quantity field
 * @param animId     tiledata animation id
 * @param hue        tiledata hue
 * @param lightIndex tiledata light index
 * @param width      art width in pixels, or zero when art is unavailable
 * @param height     art height in pixels, or zero when art is unavailable
 */
public record ItemTileInfo(
        int modelId,
        String name,
        boolean stackable,
        Set<String> flags,
        int weight,
        int layer,
        int count,
        int animId,
        int hue,
        int lightIndex,
        int width,
        int height
) {
    public ItemTileInfo {
        flags = Set.copyOf(flags);
    }
}
