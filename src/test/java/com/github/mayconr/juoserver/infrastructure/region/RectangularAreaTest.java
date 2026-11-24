package com.github.mayconr.juoserver.infrastructure.region;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RectangularAreaTest {

    @Test
    void randomLocationStaysInsideAndReachesEveryTile() {
        var area = new RectangularArea(100, 200, 3, 2);
        var random = new SplittableRandom(3);
        Set<String> tiles = new HashSet<>();

        for (int i = 0; i < 500; i++) {
            var location = area.randomLocation(random);
            assertTrue(area.contains(location), location.toString());
            assertEquals(0, location.getZ());
            tiles.add(location.getX() + "," + location.getY());
        }

        assertEquals(6, tiles.size());
    }
}
