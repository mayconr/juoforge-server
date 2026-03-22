package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.model.event.MobileRegionChanged;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegionTransitionServiceImpl implements RegionTransitionService {

    private final RegionSystem regionSystem;
    private final EventBus eventBus;

    @Override
    public void handle(MobileMoved event) {
        var oldRegion = regionSystem.getRegion(event.result().from()).orElse(null);
        var newRegion = regionSystem.getRegion(event.result().to()).orElse(null);

        if (oldRegion == null || newRegion == null) {
            return;
        }

        if (!oldRegion.equals(newRegion)) {
            eventBus.publish(new MobileRegionChanged(event.mobile(), oldRegion, newRegion));
        }
    }
}
