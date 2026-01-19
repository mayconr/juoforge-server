package com.github.mayconr.juoserver.game.model;

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
