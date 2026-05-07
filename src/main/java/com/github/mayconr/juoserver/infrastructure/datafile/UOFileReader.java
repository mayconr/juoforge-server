package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoserver.game.model.LandTile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.Static;

import java.util.List;

public interface UOFileReader {
    LandTile getLandTile(Location location);
    LandTile getLandTile(int x, int y);
    List<Static> getStatics(Location location);
    List<Static> getStatics(int x, int y);

    boolean hasBlockingStatics(int x, int y, int z);
}
