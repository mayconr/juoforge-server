package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementCollisionService {

    private final GameDataProvider gameDataProvider;

    public boolean hasBlockingCollision(UOMobile mobile, int x, int y, int z) {
        final int mobileBottom = z;
        final int mobileTop = z + 16;

        StaticTile walkableSurface = null;

        for (StaticTile statics : gameDataProvider.staticsAt(x, y)) {

            final int staticBottom = statics.z();
            final int staticTop = staticBottom + statics.height();
            final boolean intersects = mobileBottom < staticTop && mobileTop > staticBottom;
            final boolean walkable = statics.flags().contains(TileFlag.SURFACE) || statics.flags().contains(TileFlag.BRIDGE);

            /*
             * Candidate walkable surface
             */
            if (walkable) {
                final int step = Math.abs(z - staticBottom);
                /*
                 * Reachable surface
                 */
                if (step <= 16) {
                    /*
                     * Prefer highest surface
                     */
                    if (walkableSurface == null || staticBottom > walkableSurface.z()) {
                        walkableSurface = statics;
                    }
                }
            }

            /*
             * Real blocking collision
             */
            if (statics.flags().contains(TileFlag.IMPASSABLE) && intersects) {
                /*
                 * Ignore if standing on valid surface above/below
                 */
                if (walkableSurface != null && walkableSurface.z() >= staticBottom) {
                    continue;
                }
                return true;
            }
        }

        // Check if the tile does not have blocking
        var tile = gameDataProvider.tileAt(x, y);
        return tile.flags().contains(TileFlag.IMPASSABLE);
    }
}
