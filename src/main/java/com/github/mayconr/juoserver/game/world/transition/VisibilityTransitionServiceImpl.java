package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.MobileEnteredLineOfSight;
import com.github.mayconr.juoserver.game.model.event.MobileLeftLineOfSight;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class VisibilityTransitionServiceImpl implements VisibilityTransitionService {

    private final RealmStorage storage;
    private final EventBus eventBus;
    private final GamePlaySettings settings;

    @Override
    public void handle(MobileMoved event) {
        var mobile = event.mobile();

        var oldView = storage.getMobilesInRange(event.result().from(), settings.world().lightOfSight(), UOMobile::isAlive);
        var newView = storage.getMobilesInRange(event.result().to(), settings.world().lightOfSight(), UOMobile::isAlive);

        detectEnteredVisibleArea(mobile, oldView, newView);
        detectLeftVisibleArea(mobile, oldView, newView);
    }

    private void detectEnteredVisibleArea(UOMobile mobile, List<UOMobile> oldView, List<UOMobile> newView) {
        for (UOMobile target : newView) {
            if (target == mobile) continue;

            if (!oldView.contains(target)) {
                eventBus.publish(new MobileEnteredLineOfSight(mobile, target));
            }
        }
    }

    private void detectLeftVisibleArea(UOMobile mobile, List<UOMobile> oldView, List<UOMobile> newView) {
        for (UOMobile target : oldView) {
            if (target == mobile) continue;

            if (!newView.contains(target)) {
                eventBus.publish(new MobileLeftLineOfSight(mobile, target));
            }
        }
    }
}
