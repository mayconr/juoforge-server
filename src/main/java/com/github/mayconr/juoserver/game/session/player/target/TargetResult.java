package com.github.mayconr.juoserver.game.session.player.target;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record TargetResult(UOMobile sender, TargetType type, int serialId, int modelId, Location location) {
}
