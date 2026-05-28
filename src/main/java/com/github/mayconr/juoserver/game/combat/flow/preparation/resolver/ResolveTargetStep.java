package com.github.mayconr.juoserver.game.combat.flow.preparation.resolver;

import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ResolveTargetStep extends AbstractFlowStep<CombatPreparationContext> {

    private final RealmStorage storage;

    public ResolveTargetStep(RealmStorage storage) {
        super("ResolveTargetStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(CombatPreparationContext context) {
        var targetMobile = storage.getMobile(context.getTargetSerial())
                .orElse(null);
        if (targetMobile == null) {
            return StepResult.failure("Target not found");
        }
        context.setTargetMobile(targetMobile);
        return StepResult.success();
    }
}
