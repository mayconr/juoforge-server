package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;

import java.util.List;
import java.util.Optional;

public interface WorldView {

    Optional<UOMobile> getMobileBySerialId(int serial);

    List<UOMobile> mobilesInRange(Location location, int range);

    List<UOItem> itemsInRange(Location location, int range);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<Container> getContainerBySerialId(int serial);

    List<Static> getStatics(int x, int y);

    LandTile getLandTile(int x, int y);

    LandTile getLandTile(Location location);
}
