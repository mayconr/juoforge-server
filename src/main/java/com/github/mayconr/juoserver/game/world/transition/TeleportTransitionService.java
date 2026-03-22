package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.model.event.MobileRegionChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;

public interface TeleportTransitionService extends EventRegistry<MobileRegionChanged> {
}
