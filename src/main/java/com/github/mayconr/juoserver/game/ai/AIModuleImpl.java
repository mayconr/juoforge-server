package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.ai.session.AISessionManager;
import com.github.mayconr.juoserver.game.model.UONpc;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AIModuleImpl implements AIModule {

    private final AISessionManager sessionHandler;

    @Override
    public void update(double delta) {
        sessionHandler.update(delta);
    }

    @Override
    public AISession attach(UONpc npc) {
        return sessionHandler.attach(npc);
    }

    @Override
    public void detach(UONpc npc) {
        sessionHandler.detach(npc);
    }
}
