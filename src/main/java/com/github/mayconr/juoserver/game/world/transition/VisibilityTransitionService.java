package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;

public interface VisibilityTransitionService extends EventHandler<MobileMoved> {
}
