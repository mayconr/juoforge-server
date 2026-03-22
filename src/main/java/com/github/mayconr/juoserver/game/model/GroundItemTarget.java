package com.github.mayconr.juoserver.game.model;

public record GroundItemTarget(Location location) implements ItemTarget {

    public static GroundItemTarget of(Location location) {
        return new GroundItemTarget(location);
    }

}
