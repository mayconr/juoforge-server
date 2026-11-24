package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;

public record RegionTeleport(String kind, Destination destination) {
    public record Destination(Integer x, Integer y, Integer z) {
        public PointInTheWorld toLocation() {
            return new PointInTheWorld(x, y, z);
        }
    }
}
