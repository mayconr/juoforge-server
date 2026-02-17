package com.github.mayconr.juoserver.game.world.module.ai;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.module.ai.session.AISession;
import com.github.mayconr.juoserver.game.world.module.ai.session.AISessionHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AIModule implements WorldModule, AICommands {

    private final EventBus eventBus;
    private final AISessionHandler sessionHandler;

    @Override
    public void update(double delta) {
        sessionHandler.update(delta);
    }

    @Override
    public AISession attach(UONpc npc) {
        return sessionHandler.attach(npc);
    }
}
