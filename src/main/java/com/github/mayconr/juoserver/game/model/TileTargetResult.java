package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;

import java.util.List;

public record TileTargetResult(UOPlayer source, Location location, LandTile landTile, List<StaticTile> staticsTile) implements TargetResult {
}
