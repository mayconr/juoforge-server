package com.github.mayconr.juoserver.game.map;

import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.List;
import java.util.Optional;

/** Map and region queries, without file loading or infrastructure configuration. */
public interface WorldMap {
    LandTile getLandTile(Location location);

    LandTile getLandTile(int x, int y);

    List<StaticTile> getStatics(Location location);

    List<StaticTile> getStatics(int x, int y);

    Optional<RegionNode> getRegion(String name);

    /** Resolves the most specific region containing the location. */
    Optional<RegionNode> getRegion(Location location);

    List<RegionNode> getRegionsByType(RegionType type);
}
