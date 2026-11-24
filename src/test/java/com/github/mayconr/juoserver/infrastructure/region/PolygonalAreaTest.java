package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolygonalAreaTest {
    private static final List<AreaPoint> L_SHAPE = List.of(
            new AreaPoint(0, 0),
            new AreaPoint(4, 0),
            new AreaPoint(4, 2),
            new AreaPoint(2, 2),
            new AreaPoint(2, 4),
            new AreaPoint(0, 4)
    );

    @TempDir
    Path directory;

    @Test
    void coversSameTilesAsEquivalentRectangle() {
        var polygon = new PolygonalArea(List.of(
                new AreaPoint(10, 20),
                new AreaPoint(15, 20),
                new AreaPoint(15, 23),
                new AreaPoint(10, 23)
        ));
        var rectangle = new RectangularArea(10, 20, 5, 3);

        for (int x = 8; x < 18; x++) {
            for (int y = 18; y < 26; y++) {
                var location = new PointInTheWorld(x, y, 0);
                assertEquals(rectangle.contains(location), polygon.contains(location), location.toString());
            }
        }
        assertEquals(15, polygon.tileCount());
    }

    @Test
    void excludesConcaveCorner() {
        var area = new PolygonalArea(L_SHAPE);

        assertTrue(area.contains(new PointInTheWorld(3, 1, 0)));
        assertTrue(area.contains(new PointInTheWorld(1, 3, 0)));
        assertFalse(area.contains(new PointInTheWorld(3, 3, 0)));
        assertFalse(area.contains(new PointInTheWorld(4, 1, 0)));
        assertEquals(12, area.tileCount());
    }

    @Test
    void randomLocationStaysInsideAndReachesEveryTile() {
        var area = new PolygonalArea(L_SHAPE);
        var random = new SplittableRandom(7);
        Map<String, Integer> hits = new HashMap<>();

        for (int i = 0; i < 2_000; i++) {
            var location = area.randomLocation(random);
            assertTrue(area.contains(location), location.toString());
            assertEquals(0, location.getZ());
            hits.merge(location.getX() + "," + location.getY(), 1, Integer::sum);
        }

        assertEquals(12, hits.size());
    }

    @Test
    void randomLocationSupportsTriangles() {
        var area = new PolygonalArea(List.of(new AreaPoint(0, 0), new AreaPoint(20, 0), new AreaPoint(0, 20)));
        var random = new SplittableRandom(11);

        for (int i = 0; i < 1_000; i++) {
            var location = area.randomLocation(random);
            assertTrue(area.contains(location), location.toString());
            assertTrue(location.getX() + location.getY() < 20, location.toString());
        }
    }

    @Test
    void centerIsInsideConcavePolygon() {
        var area = new PolygonalArea(List.of(
                new AreaPoint(0, 0),
                new AreaPoint(10, 0),
                new AreaPoint(10, 2),
                new AreaPoint(2, 2),
                new AreaPoint(2, 8),
                new AreaPoint(10, 8),
                new AreaPoint(10, 10),
                new AreaPoint(0, 10)
        ));

        assertTrue(area.contains(area.getCenter()));
    }

    @Test
    void rejectsPolygonsWithoutTiles() {
        assertThrows(IllegalArgumentException.class,
                () -> new PolygonalArea(List.of(new AreaPoint(0, 0), new AreaPoint(1, 0))));
        assertThrows(IllegalArgumentException.class,
                () -> new PolygonalArea(List.of(new AreaPoint(0, 0), new AreaPoint(5, 0), new AreaPoint(10, 0))));
    }

    @Test
    void loadsPolygonAreaFromRegionJson() throws Exception {
        Files.writeString(directory.resolve("regions.json"), """
                [{"name":"cave","displayName":"Cave","type":"DUNGEON",
                  "area":{"type":"polygon","points":[{"x":0,"y":0},{"x":4,"y":0},{"x":4,"y":2},
                                                     {"x":2,"y":2},{"x":2,"y":4},{"x":0,"y":4}]}}]
                """);

        var regions = new RegionSystemImpl(new JsonTemplateLoader<>(directory, RegionTemplate.class));
        var area = regions.getRegion("cave").orElseThrow().getArea();

        assertEquals(new PolygonalArea(L_SHAPE), assertInstanceOf(PolygonalArea.class, area));
    }
}
