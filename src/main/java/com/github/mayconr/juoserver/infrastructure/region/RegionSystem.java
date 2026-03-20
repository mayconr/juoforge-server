package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;

import java.util.List;
import java.util.Optional;

public interface RegionSystem {

    Optional<RegionNode> getRegion(String name);

    Optional<RegionNode> getRegion(Location location);

    List<RegionNode> getRegionsByType(RegionType type);
}

