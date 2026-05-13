package com.github.mayconr.juoserver.game.shared.step;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface LightOfSightContext {
    UOPlayer targetSource();
    Location targetDestination();
}
