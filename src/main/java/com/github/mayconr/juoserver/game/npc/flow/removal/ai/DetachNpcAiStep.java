package com.github.mayconr.juoserver.game.npc.flow.removal.ai;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DetachNpcAiStep extends AbstractFlowStep<NpcRemovalContext> {

    private final AIModule aiModule;

    public DetachNpcAiStep(AIModule aiModule) {
        super("DetachNpcAi");
        this.aiModule = aiModule;
    }

    @Override
    public StepResult execute(NpcRemovalContext context) {
        aiModule.detach(context.getNpc());
        return StepResult.success();
    }
}
