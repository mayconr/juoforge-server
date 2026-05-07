package com.github.mayconr.juoserver.game.shared.step;

import com.github.mayconr.juoserver.game.model.Location;

public interface LightOfSightContext {
    Location targetSource();
    Location targetDestination();
}
