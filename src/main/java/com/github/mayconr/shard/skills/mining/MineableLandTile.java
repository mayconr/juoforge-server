package com.github.mayconr.shard.skills.mining;

import java.util.Arrays;

public enum MineableLandTile {
    ROCK(220, 223),
    MOUNTAIN(227, 230),
    CAVE(240, 252),
    SAND(22, 75);

    private final int min;
    private final int max;

    MineableLandTile(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean matches(int landTileId) {
        return landTileId >= min && landTileId <= max;
    }

    public static boolean isMineable(int landTileId) {
        return Arrays.stream(values()).anyMatch(v -> v.matches(landTileId));
    }
}
