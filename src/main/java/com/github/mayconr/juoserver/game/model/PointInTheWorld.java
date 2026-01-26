package com.github.mayconr.juoserver.game.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PointInTheWorld implements Location {

    private final int x;
    private final int y;
    private final int z;

    public PointInTheWorld(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public PointInTheWorld(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }
    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }
}
