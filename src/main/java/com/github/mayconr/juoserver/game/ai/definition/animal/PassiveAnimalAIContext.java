package com.github.mayconr.juoserver.game.ai.definition.animal;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;

public class PassiveAnimalAIContext extends AIFlowContext {

    public PassiveAnimalAIContext(UONpc npc, World world) {
        super(npc, world);
    }
}
