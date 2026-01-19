package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.session.game.GameSession;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;

public interface NpcAI extends GameTask {

    void initialize(GameSession gameSession, NpcSession npcSession);
}
