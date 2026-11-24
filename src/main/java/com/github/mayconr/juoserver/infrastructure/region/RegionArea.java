package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;

import java.util.random.RandomGenerator;

public interface RegionArea {

    boolean contains(Location location);

    Location getCenter();

    /**
     * Picks a tile inside the area with uniform probability.
     *
     * <p>The area is two-dimensional, so the returned location has {@code z = 0}; callers resolve
     * the height from the map at that point.
     *
     * @param random source of randomness
     * @return a location for which {@link #contains(Location)} is {@code true}
     */
    Location randomLocation(RandomGenerator random);
}
