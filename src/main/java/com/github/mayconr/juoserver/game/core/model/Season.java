package com.github.mayconr.juoserver.game.core.model;

public enum Season {
    Spring,
    Summer,
    Fall,
    Winter,
    Desolation;

    public int getCode() {
        return ordinal();
    }
}
