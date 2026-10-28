package com.github.mayconr.juoserver.game.ai.registry;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;

@FunctionalInterface
public interface AIContextFactory<T extends AIFlowContext> {
    T create(UONpc npc, World world);
}
