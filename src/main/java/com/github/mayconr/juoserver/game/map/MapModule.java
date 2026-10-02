package com.github.mayconr.juoserver.game.map;

import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MapModule implements WorldMap, WorldModule {
    private final UOFileReader fileReader;
    private final RegionSystem regions;

    @Override
    public LandTile getLandTile(Location location) {
        return fileReader.getLandTile(location);
    }

    @Override
    public LandTile getLandTile(int x, int y) {
        return fileReader.getLandTile(x, y);
    }

    @Override
    public List<StaticTile> getStatics(Location location) {
        return fileReader.getStatics(location);
    }

    @Override
    public List<StaticTile> getStatics(int x, int y) {
        return fileReader.getStatics(x, y);
    }

    @Override
    public Optional<RegionNode> getRegion(String name) {
        return regions.getRegion(name);
    }

    @Override
    public Optional<RegionNode> getRegion(Location location) {
        return regions.getRegion(location);
    }

    @Override
    public List<RegionNode> getRegionsByType(RegionType type) {
        return regions.getRegionsByType(type);
    }
}
