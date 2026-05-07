package com.github.mayconr.juoserver.game.model;

import java.util.List;

public record StaticTargetResult(UOPlayer source, Location location, LandTile landTile, List<Static> statics) implements TargetResult {
}
