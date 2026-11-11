package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Resolves the Z a walking mobile reaches when it steps from a location towards a direction.
 * <p>
 * The client predicts the destination Z on its own and only receives the server Z when a step
 * is denied or the player is redrawn, so the server must reach the same value: land uses the
 * average of its four corners, surfaces are stood on at their top ({@code z + height}, halved
 * for bridges), a step may climb {@value #STEP_HEIGHT} above the current surface and the mobile
 * needs {@value #PERSON_HEIGHT} of clearance.
 */
@RequiredArgsConstructor
public class WalkSurfaceResolver {

    static final int PERSON_HEIGHT = 16;
    static final int STEP_HEIGHT = 2;

    private final GameDataProvider gameDataProvider;

    /**
     * @return the destination Z, or empty when the step is blocked
     */
    public OptionalInt resolveWalkZ(Location from, Direction direction) {
        final var start = startSurface(from.getX(), from.getY(), from.getZ());
        if (start.isEmpty()) {
            return OptionalInt.empty();
        }

        final var forwardZ = resolveStepZ(from, direction, start.get());
        if (forwardZ.isEmpty() || !isDiagonal(direction)) {
            return forwardZ;
        }

        return canPassDiagonalCorners(from, direction, start.get()) ? forwardZ : OptionalInt.empty();
    }

    private boolean isDiagonal(Direction direction) {
        return (direction.getCode() & 0x1) == 0x1;
    }

    /**
     * Diagonal steps must not cut corners: both adjacent cardinal tiles must be walkable too.
     */
    private boolean canPassDiagonalCorners(Location from, Direction direction, StartSurface start) {
        final var left = Direction.fromCode((direction.getCode() - 1) & 0x7);
        final var right = Direction.fromCode((direction.getCode() + 1) & 0x7);

        return resolveStepZ(from, left, start).isPresent() && resolveStepZ(from, right, start).isPresent();
    }

    private OptionalInt resolveStepZ(Location from, Direction direction, StartSurface start) {
        final int x = from.getX() + direction.getDx();
        final int y = from.getY() + direction.getDy();

        return landAt(x, y)
                .map(land -> resolveDestinationZ(x, y, from.getZ(), land, start))
                .orElse(OptionalInt.empty());
    }

    private OptionalInt resolveDestinationZ(int x, int y, int currentZ, LandSample land, StartSurface start) {
        final var statics = gameDataProvider.staticsAt(x, y);
        final var step = new StepLimits(start.top() + STEP_HEIGHT, start.z() + PERSON_HEIGHT, currentZ);

        final var staticZ = resolveStaticSurfaceZ(statics, land, step);
        if (!canStandOnLand(land, step)) {
            return staticZ;
        }

        final var landZ = resolveLandZ(statics, land, step, staticZ);
        return landZ.isPresent() ? landZ : staticZ;
    }

    private OptionalInt resolveStaticSurfaceZ(List<StaticTile> statics, LandSample land, StepLimits step) {
        OptionalInt resolved = OptionalInt.empty();

        for (StaticTile surface : statics) {
            if (!isWalkableSurface(surface)) {
                continue;
            }

            final int ourZ = surface.z() + calcHeight(surface);
            if (resolved.isPresent() && !isPreferred(ourZ, resolved.getAsInt(), step.currentZ())) {
                continue;
            }

            if (canStepOnto(statics, surface, ourZ, land, step)) {
                resolved = OptionalInt.of(ourZ);
            }
        }

        return resolved;
    }

    private boolean canStepOnto(List<StaticTile> statics, StaticTile surface, int ourZ, LandSample land, StepLimits step) {
        final int testTop = Math.max(step.checkTop(), ourZ + PERSON_HEIGHT);
        final int itemTop = surface.flags().contains(TileFlag.BRIDGE) ? surface.z() : surface.z() + surface.height();

        if (step.stepTop() < itemTop) {
            return false;
        }

        if (isBuriedByLand(surface, ourZ, testTop, land)) {
            return false;
        }

        return hasClearance(statics, ourZ, testTop);
    }

    /**
     * A thin surface below the land center (e.g. a floor tile partially under a hill) cannot be stood on.
     */
    private boolean isBuriedByLand(StaticTile surface, int ourZ, int testTop, LandSample land) {
        final int landCheck = surface.z() + Math.min(surface.height(), STEP_HEIGHT);

        return !land.ignored() && landCheck < land.averageZ() && land.averageZ() > ourZ && testTop > land.lowZ();
    }

    private boolean canStandOnLand(LandSample land, StepLimits step) {
        return !land.ignored() && !land.impassable() && step.stepTop() >= land.lowZ();
    }

    private OptionalInt resolveLandZ(List<StaticTile> statics, LandSample land, StepLimits step, OptionalInt staticZ) {
        final int landZ = land.averageZ();

        if (staticZ.isPresent() && !isPreferred(landZ, staticZ.getAsInt(), step.currentZ())) {
            return OptionalInt.empty();
        }

        final int testTop = Math.max(step.checkTop(), landZ + PERSON_HEIGHT);
        return hasClearance(statics, landZ, testTop) ? OptionalInt.of(landZ) : OptionalInt.empty();
    }

    /**
     * Among reachable surfaces the one closest to the current Z wins; ties go to the lowest.
     */
    private boolean isPreferred(int candidateZ, int resolvedZ, int currentZ) {
        final int cmp = Math.abs(candidateZ - currentZ) - Math.abs(resolvedZ - currentZ);

        return cmp < 0 || cmp == 0 && candidateZ <= resolvedZ;
    }

    private boolean hasClearance(List<StaticTile> statics, int ourZ, int ourTop) {
        for (StaticTile tile : statics) {
            if (!isImpassableOrSurface(tile)) {
                continue;
            }

            final int checkTop = tile.z() + calcHeight(tile);
            if (checkTop > ourZ && ourTop > tile.z()) {
                return false;
            }
        }

        return true;
    }

    private Optional<StartSurface> startSurface(int x, int y, int z) {
        return landAt(x, y).map(land -> startSurface(x, y, z, land));
    }

    /**
     * Finds the surface the mobile is standing on: its base ({@code z}) and its top, used to
     * limit how high the next step can climb.
     */
    private StartSurface startSurface(int x, int y, int z, LandSample land) {
        var surface = landStartSurface(z, land);

        for (StaticTile tile : gameDataProvider.staticsAt(x, y)) {
            surface = staticStartSurface(z, tile, surface);
        }

        if (surface == null) {
            return new StartSurface(z, z, z);
        }

        return new StartSurface(surface.z(), surface.center(), Math.max(surface.top(), z));
    }

    private StartSurface landStartSurface(int z, LandSample land) {
        if (land.ignored() || land.impassable() || z < land.averageZ()) {
            return null;
        }

        return new StartSurface(land.lowZ(), land.averageZ(), land.topZ());
    }

    private StartSurface staticStartSurface(int z, StaticTile tile, StartSurface current) {
        final int calcTop = tile.z() + calcHeight(tile);

        if (!tile.flags().contains(TileFlag.SURFACE) || z < calcTop) {
            return current;
        }

        if (current != null && calcTop < current.center()) {
            return current;
        }

        final int top = tile.z() + tile.height();
        final int resolvedTop = current == null ? top : Math.max(current.top(), top);
        return new StartSurface(tile.z(), calcTop, resolvedTop);
    }

    private Optional<LandSample> landAt(int x, int y) {
        final var tile = gameDataProvider.tileAt(x, y);
        final var left = gameDataProvider.tileAt(x, y + 1);
        final var right = gameDataProvider.tileAt(x + 1, y);
        final var bottom = gameDataProvider.tileAt(x + 1, y + 1);

        if (tile == null || left == null || right == null || bottom == null) {
            return Optional.empty();
        }

        return Optional.of(LandSample.of(tile, left.z(), right.z(), bottom.z()));
    }

    private boolean isWalkableSurface(StaticTile tile) {
        return tile.flags().contains(TileFlag.SURFACE) && !tile.flags().contains(TileFlag.IMPASSABLE);
    }

    private boolean isImpassableOrSurface(StaticTile tile) {
        return tile.flags().contains(TileFlag.IMPASSABLE) || tile.flags().contains(TileFlag.SURFACE);
    }

    private int calcHeight(StaticTile tile) {
        return tile.flags().contains(TileFlag.BRIDGE) ? tile.height() / 2 : tile.height();
    }

    private record StepLimits(int stepTop, int checkTop, int currentZ) {
    }

    private record StartSurface(int z, int center, int top) {
    }

    private record LandSample(int lowZ, int averageZ, int topZ, boolean impassable, boolean ignored) {

        static LandSample of(LandTile tile, int leftZ, int rightZ, int bottomZ) {
            final int topZ = tile.z();
            final int low = Math.min(Math.min(topZ, leftZ), Math.min(rightZ, bottomZ));
            final int high = Math.max(Math.max(topZ, leftZ), Math.max(rightZ, bottomZ));

            return new LandSample(
                    low,
                    averageZ(topZ, leftZ, rightZ, bottomZ),
                    high,
                    tile.flags().contains(TileFlag.IMPASSABLE),
                    isIgnored(tile.id())
            );
        }

        /**
         * The land center follows the diagonal with the smallest slope.
         */
        private static int averageZ(int topZ, int leftZ, int rightZ, int bottomZ) {
            if (Math.abs(topZ - bottomZ) > Math.abs(leftZ - rightZ)) {
                return Math.floorDiv(leftZ + rightZ, 2);
            }

            return Math.floorDiv(topZ + bottomZ, 2);
        }

        /**
         * NoDraw and void land graphics are not part of the walkable terrain.
         */
        private static boolean isIgnored(int landId) {
            return landId == 0x2 || landId == 0x1DB || landId >= 0x1AE && landId <= 0x1B5;
        }
    }
}
