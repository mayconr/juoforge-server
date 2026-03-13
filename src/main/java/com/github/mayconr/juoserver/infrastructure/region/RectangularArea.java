package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;

import java.util.Objects;

public record RectangularArea(int x, int y, int width, int height) implements RegionArea {

    public RectangularArea {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than zero.");
        }

    }

    @Override
    public boolean contains(Location location) {
        Objects.requireNonNull(location, "location must not be null");

        int lx = location.getX();
        int ly = location.getY();

        return lx >= x &&
                ly >= y &&
                lx < (x + width) &&
                ly < (y + height);
    }

    @Override
    public Location getCenter() {
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        return new PointInTheWorld(centerX, centerY, 0);
    }
}
