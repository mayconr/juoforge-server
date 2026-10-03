package com.github.mayconr.juoserver.game.npc.flow.creation.resolve;

import com.github.mayconr.juoserver.game.npc.ai.NpcAIProfileResolver;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ResolveNpcAIProfileStep extends AbstractFlowStep<NpcCreationContext> {
    private final NpcAIProfileResolver resolver;

    public ResolveNpcAIProfileStep(NpcAIProfileResolver resolver) {
        super("ResolveNpcAIProfile");
        this.resolver = resolver;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        context.setBehavior(resolver.resolve(context.getTemplate()));
        return StepResult.success();
    }
}
