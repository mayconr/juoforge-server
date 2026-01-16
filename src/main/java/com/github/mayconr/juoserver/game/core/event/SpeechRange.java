package com.github.mayconr.juoserver.game.core.event;

public enum SpeechRange {
    SELF(0),
    SHORT(2),
    NORMAL(8),
    LONG(15),
    GLOBAL(Integer.MAX_VALUE);

    private final int tiles;

    SpeechRange(int tiles) {
        this.tiles = tiles;
    }

    public int tiles() {
        return tiles;
    }
}
