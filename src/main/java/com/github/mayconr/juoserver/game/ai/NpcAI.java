package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.npc.NpcSession;

public interface NpcAI extends GameTask {

    void initialize(WorldInternal worldInternal, NpcSession npcSession);
}
