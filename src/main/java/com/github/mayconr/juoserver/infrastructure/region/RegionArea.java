package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;

public interface RegionArea {

    boolean contains(Location location);
}
