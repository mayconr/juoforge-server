package com.github.mayconr.juoserver.game.world.transition;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class DespawnNpcOnDeath implements EventRegistry<MobileDeathEvent> {

    private final MobileModule mobileModule;
    private final AIModule aiModule;

    @Override
    public void handle(MobileDeathEvent event) {
        if (event.target() instanceof UONpc npc) {
            aiModule.detach(npc);
            mobileModule.scheduleDespawn(npc, 20);
        }
    }

    @Override
    public Class<MobileDeathEvent> getType() {
        return MobileDeathEvent.class;
    }

    @Override
    public Predicate<MobileDeathEvent> getPredicate() {
        return event -> event.target() instanceof UONpc;
    }
}
