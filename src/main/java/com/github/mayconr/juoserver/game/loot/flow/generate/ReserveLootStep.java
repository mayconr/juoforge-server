package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.List;

public final class ReserveLootStep extends AbstractFlowStep<GenerateLootContext> {
    private static final String ATTEMPTED = "loot.generationAttempted";

    public ReserveLootStep() {
        super("reserve-loot-attempt");
    }

    @Override
    public StepResult execute(GenerateLootContext context) {
        var corpse = context.getRequest().corpse();
        synchronized (corpse) {
            if (corpse.persistentAttributes().getOrDefault(ATTEMPTED, false)) {
                context.complete(List.of());
                return StepResult.stop("LOOT_ALREADY_ATTEMPTED", "Loot generation was already attempted");
            }
            // Reserve before invoking shard code, including when a later step fails.
            corpse.persistentAttributes().set(ATTEMPTED, true);
        }
        return StepResult.success();
    }
}
