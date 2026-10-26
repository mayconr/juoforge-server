package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.game.loot.LootPolicy;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.List;

public final class ResolveLootStep extends AbstractFlowStep<GenerateLootContext> {
    private final LootPolicy policy;
    private final RNG rng;

    public ResolveLootStep(LootPolicy policy, RNG rng) {
        super("resolve-loot");
        this.policy = policy;
        this.rng = rng;
    }

    @Override
    public StepResult execute(GenerateLootContext context) {
        context.setItems(List.copyOf(policy.resolve(context.getRequest(), rng)));
        return StepResult.success();
    }
}
