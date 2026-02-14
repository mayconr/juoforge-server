package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.region.RegionQueryService;

import java.util.List;
import java.util.Optional;

public interface WorldView extends RegionQueryService {

    Optional<UOMobile> getMobileBySerialId(int serial);

    List<UOItem> itemsInRange(Location location, int range);

    List<UOMobile> getMobilesInRange(Location location, int radius);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<Container> getContainerBySerialId(int serial);

    List<Static> getStatics(int x, int y);

    List<Static> getStatics(Location location);

    LandTile getLandTile(int x, int y);

    LandTile getLandTile(Location location);

    List<UOPlayer> getOnlinePlayers();
}
