package com.github.mayconr.shard.region;

import com.github.mayconr.juoserver.game.model.event.MobileRegionChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

@Slf4j
public final class MobileRegionChangedListener implements EventRegistry<MobileRegionChanged> {
    @Override
    public void handle(MobileRegionChanged event) {
        log.info("Mobile {} changed region: origin={} destination={}",
                event.mobile().getSerialId(), event.oldRegion().getName(), event.newRegion().getName());
    }

    @Override
    public Class<MobileRegionChanged> getType() {
        return MobileRegionChanged.class;
    }

    @Override
    public Predicate<MobileRegionChanged> getPredicate() {
        return event -> true;
    }
}
