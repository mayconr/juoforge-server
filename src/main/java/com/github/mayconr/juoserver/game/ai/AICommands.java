package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.ai.session.AISession;

public interface AICommands {

    AISession attach(UONpc npc);

    void detach(UONpc npc);
}
