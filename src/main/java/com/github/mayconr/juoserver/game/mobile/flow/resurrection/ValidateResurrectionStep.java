package com.github.mayconr.juoserver.game.mobile.flow.resurrection;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateResurrectionStep extends AbstractFlowStep<ResurrectionContext> {
    public ValidateResurrectionStep() {
        super("validate-resurrection");
    }

    @Override
    public StepResult execute(ResurrectionContext context) {
        var mobile = context.getMobile();
        if (mobile == null) {
            return StepResult.failure("MISSING_MOBILE", "Resurrection target is required");
        }
        if (!(mobile instanceof UOPlayer)) {
            return StepResult.failure("NOT_PLAYER", "Only players can be resurrected");
        }
        if (mobile.isAlive()) {
            return StepResult.failure("ALREADY_ALIVE", "Player is already alive");
        }
        if (mobile.getMaxHitpoints() <= 0 || mobile.getMaxStamina() < 0 || mobile.getMaxMana() < 0) {
            return StepResult.failure("INVALID_VITALS", "Player has invalid maximum vitals");
        }
        return StepResult.success();
    }
}

