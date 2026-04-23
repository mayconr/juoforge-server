package com.github.mayconr.juoserver.game.npc.flow.removal;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class DetachNpcAiStep extends AbstractFlowStep<NpcRemovalFlowDefinition.NpcRemovalContext> {

    private final AIModule aiModule;

    public DetachNpcAiStep(int order, AIModule aiModule) {
        super("DetachNpcAi", order, FlowPhase.CORE);
        this.aiModule = aiModule;
    }

    @Override
    public StepResult execute(NpcRemovalFlowDefinition.NpcRemovalContext context) {
        aiModule.detach(context.getNpc());
        return StepResult.success();
    }
}
