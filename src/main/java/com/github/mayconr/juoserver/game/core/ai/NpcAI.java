package com.github.mayconr.juoserver.game.core.ai;

import com.github.mayconr.juoserver.game.core.gameloop.GameTask;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSession;

public interface NpcAI extends GameTask {

    void initialize(GameSession gameSession, NpcSession npcSession);
}
