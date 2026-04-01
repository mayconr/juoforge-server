package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.DeathRequest;
import com.github.mayconr.juoserver.game.model.LethalDamageEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class LethalDamageToDeath implements EventRegistry<LethalDamageEvent> {

    private final MobileModule mobileModule;

    @Override
    public Class<LethalDamageEvent> getType() {
        return LethalDamageEvent.class;
    }

    @Override
    public Predicate<LethalDamageEvent> getPredicate() {
        return event->true;
    }

    @Override
    public void handle(LethalDamageEvent event) {
        mobileModule.die(new DeathRequest(event.target(), event.source(), DeathCause.COMBAT));
    }
}
