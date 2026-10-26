package com.github.mayconr.juoserver.game.mobile.flow.resurrection;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class RestorePlayerLifeStep extends AbstractFlowStep<ResurrectionContext> {
    public RestorePlayerLifeStep() {
        super("restore-player-life");
    }

    @Override
    public StepResult execute(ResurrectionContext context) {
        var mobile = context.getMobile();
        mobile.setHitpoints(Math.min(5, mobile.getMaxHitpoints()));
        mobile.setStamina(Math.min(5, mobile.getMaxStamina()));
        mobile.setMana(Math.min(5, mobile.getMaxMana()));
        mobile.setAlive(true);
        return StepResult.success();
    }
}

