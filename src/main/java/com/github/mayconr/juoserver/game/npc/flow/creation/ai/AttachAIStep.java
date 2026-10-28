package com.github.mayconr.juoserver.game.npc.flow.creation.ai;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class AttachAIStep extends AbstractFlowStep<NpcCreationContext> {

    private final AIModule aiModule;

    public AttachAIStep(AIModule aiModule) {
        super("AttachAI");
        this.aiModule = aiModule;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        aiModule.attach(context.getNpc());
        return StepResult.success();
    }
}
