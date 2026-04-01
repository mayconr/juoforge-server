package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileDamagedEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DamageModuleImpl implements DamageModule {

    private final EventBus eventBus;

    @Override
    public void applyDamage(DamageRequest request) {
        final var target = request.target();

        int totalDamage = 0;
        for (DamageComponent damage : request.components()) {
            totalDamage += damage.damage();
        }

        int oldHitPoints = target.getHitpoints();
        target.setHitpoints( Math.max(0 , target.getHitpoints() - totalDamage) );

        if (target.getHitpoints() == 0) {
            eventBus.publish(new LethalDamageEvent(request.source(), request.target(), request.sourceKind()));
        } else {
            eventBus.publish(new MobileDamagedEvent(request.source(), target, request.sourceKind(), request.components(), totalDamage, oldHitPoints, target.getHitpoints()));
        }
    }

    @Override
    public void kill(UOMobile target, UOMobile source, DamageSourceKind kind) {
        target.setHitpoints(0);
        target.setStamina(0);
        target.setMana(0);

        eventBus.publish(new LethalDamageEvent(source, target, kind));
    }

}
