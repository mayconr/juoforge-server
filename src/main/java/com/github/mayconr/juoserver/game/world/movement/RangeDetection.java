package com.github.mayconr.juoserver.game.world.movement;

import com.github.mayconr.juoserver.ServerProperties;
import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.event.EventHandler;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.MobileEnteredLineOfSight;
import com.github.mayconr.juoserver.game.model.event.MobileLeftLineOfSight;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RangeDetection implements EventHandler<MobileMoved> {

    private final WorldInternal world;
    private final EventBus eventBus;
    private final ServerProperties properties;

    @Override
    public void handle(MobileMoved event) {
        var mobile = event.mobile();
        var oldView = world.getMobilesInRange(event.result().from(), properties.world().lightOfSight());
        var newView = world.getMobilesInRange(event.result().to(), properties.world().lightOfSight());

        detectEnteredRange(mobile, oldView, newView);
        detectLeftRange(mobile, oldView, newView);
    }

    private void detectEnteredRange(UOMobile mobile, List<UOMobile> oldView, List<UOMobile> newView) {
        for (UOMobile target : newView) {
            if (target == mobile) continue;

            if (!oldView.contains(target)) {
                eventBus.publish(new MobileEnteredLineOfSight(mobile, target));
            }
        }
    }

    private void detectLeftRange(UOMobile mobile, List<UOMobile> oldView, List<UOMobile> newView) {
        for (UOMobile target : oldView) {
            if (target == mobile) continue;

            if (!newView.contains(target)) {
                eventBus.publish(new MobileLeftLineOfSight(mobile, target));
            }
        }
    }
}
