package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.art.ArtMetadata;
import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoforge.reader.tiledata.ItemTileData;
import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WalkSurfaceResolverTest {

    private static final int GRASS = 0x3;

    private final FakeMap map = new FakeMap();
    private final WalkSurfaceResolver resolver = new WalkSurfaceResolver(map);

    @Test
    void keepsZOnFlatLand() {
        assertEquals(OptionalInt.of(0), resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.EAST));
    }

    @Test
    void followsLandAverageWhenClimbingASlope() {
        // start tile (10,10) corners: 0,0,4,4 -> center 2; destination (11,10) corners: 4,4,8,8 -> center 6
        map.landZ(11, 10, 4);
        map.landZ(11, 11, 4);
        map.landZ(12, 10, 8);
        map.landZ(12, 11, 8);

        assertEquals(OptionalInt.of(6), resolver.resolveWalkZ(new PointInTheWorld(10, 10, 2), Direction.EAST));
    }

    @Test
    void standsOnTopOfABridgeUsingHalfItsHeight() {
        map.addStatic(11, 10, 0, 10, TileFlag.SURFACE, TileFlag.BRIDGE);

        assertEquals(OptionalInt.of(5), resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.EAST));
    }

    @Test
    void cannotClimbASurfaceHigherThanTheStepHeight() {
        map.addStatic(11, 10, 0, 6, TileFlag.SURFACE);

        assertTrue(resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.EAST).isEmpty());
    }

    @Test
    void walksOnAFloorAtTheSameHeight() {
        map.addStatic(10, 10, 20, 0, TileFlag.SURFACE);
        map.addStatic(11, 10, 20, 0, TileFlag.SURFACE);

        assertEquals(OptionalInt.of(20), resolver.resolveWalkZ(new PointInTheWorld(10, 10, 20), Direction.EAST));
    }

    @Test
    void isBlockedByAnImpassableStatic() {
        map.addStatic(11, 10, 0, 20, TileFlag.IMPASSABLE);

        assertTrue(resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.EAST).isEmpty());
    }

    @Test
    void cannotCutACornerOnDiagonalSteps() {
        map.addStatic(10, 9, 0, 20, TileFlag.IMPASSABLE);

        assertTrue(resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.NORTHEAST).isEmpty());
    }

    @Test
    void isBlockedOutsideTheMap() {
        map.removeLand(11, 10);

        assertTrue(resolver.resolveWalkZ(new PointInTheWorld(10, 10, 0), Direction.EAST).isEmpty());
    }

    private static final class FakeMap implements GameDataProvider {

        @Override
        public Optional<ArtMetadata> itemArt(int itemId) {
            return Optional.empty();
        }

        @Override
        public Optional<ArtMetadata> landArt(int landId) {
            return Optional.empty();
        }


        private final Map<Long, Integer> landZ = new HashMap<>();
        private final Map<Long, List<StaticTile>> statics = new HashMap<>();
        private final Set<Long> missingLand = new HashSet<>();

        void landZ(int x, int y, int z) {
            landZ.put(key(x, y), z);
        }

        void removeLand(int x, int y) {
            missingLand.add(key(x, y));
        }

        void addStatic(int x, int y, int z, int height, TileFlag flag, TileFlag... flags) {
            statics.computeIfAbsent(key(x, y), ignored -> new ArrayList<>())
                    .add(new FakeStatic(x, y, z, height, EnumSet.of(flag, flags)));
        }

        @Override
        public LandTile tileAt(int x, int y) {
            if (missingLand.contains(key(x, y))) {
                return null;
            }
            return new FakeLand(x, y, landZ.getOrDefault(key(x, y), 0));
        }

        @Override
        public List<StaticTile> staticsAt(int x, int y) {
            return statics.getOrDefault(key(x, y), List.of());
        }

        @Override
        public Optional<Skill> skillBy(int skillId) {
            return Optional.empty();
        }

        @Override
        public Optional<ItemTileData> itemTileData(int itemId) {
            return Optional.empty();
        }

        private static long key(int x, int y) {
            return ((long) x << 32) | (y & 0xFFFFFFFFL);
        }
    }

    private record FakeLand(int x, int y, int z) implements LandTile {

        @Override
        public int id() {
            return GRASS;
        }

        @Override
        public String name() {
            return "grass";
        }

        @Override
        public Set<TileFlag> flags() {
            return EnumSet.noneOf(TileFlag.class);
        }
    }

    private record FakeStatic(int x, int y, int z, int height, Set<TileFlag> flags) implements StaticTile {

        @Override
        public int id() {
            return 0x1;
        }

        @Override
        public String name() {
            return "static";
        }
    }
}
