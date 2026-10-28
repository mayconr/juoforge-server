package com.github.mayconr.juoserver.game.npc.flow.creation.ai;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ActivateAIStep extends AbstractFlowStep<NpcCreationContext> {

    private final AIModule aiModule;

    public ActivateAIStep(AIModule aiModule) {
        super("ActivateAI");
        this.aiModule = aiModule;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        aiModule.activate(context.getNpc());
        return StepResult.success();
    }
}
