package com.github.mayconr.juoserver.game.world.module.ai;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.module.ai.session.AISession;

public interface AICommands {

    AISession attach(UONpc npc);

}
