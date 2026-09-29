package com.github.mayconr.juoserver.game.npc.flow.creation.resolve;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ResolveNpcStatsStep extends AbstractFlowStep<NpcCreationContext> {
    private final NpcStatsResolver resolver;

    public ResolveNpcStatsStep(NpcStatsResolver resolver) {
        super("ResolveNpcStats");
        this.resolver = resolver;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        context.setStats(resolver.resolve(context.getTemplate()));
        return StepResult.success();
    }
}
