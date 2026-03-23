package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.WorldModule;

public interface AIModule extends WorldModule {

    AISession attach(UONpc npc);

    void detach(UONpc npc);
}
