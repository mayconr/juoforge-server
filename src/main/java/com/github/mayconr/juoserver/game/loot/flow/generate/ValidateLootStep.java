package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateLootStep extends AbstractFlowStep<GenerateLootContext> {
    public ValidateLootStep() {
        super("validate-loot");
    }

    @Override
    public StepResult execute(GenerateLootContext context) {
        var request = context.getRequest();
        if (request == null) {
            return StepResult.failure("MISSING_LOOT_REQUEST", "Loot request is required");
        }
        if (request.victim().isAlive()) {
            return StepResult.failure("LIVING_LOOT_VICTIM", "Loot requires a dead NPC");
        }
        if (!(request.corpse().getCurrentLocation() instanceof GroundLocation)) {
            return StepResult.failure("INVALID_LOOT_CORPSE", "Loot corpse must be on the ground");
        }
        return StepResult.success();
    }
}
