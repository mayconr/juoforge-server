package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

public record MobileRegionChanged(UOMobile mobile, RegionNode oldRegion, RegionNode newRegion) implements GameEvent {
}
