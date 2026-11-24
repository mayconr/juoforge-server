package com.github.mayconr.juoserver.game.mobile.flow.setvitals;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

final class ApplyVitalsStep extends AbstractFlowStep<SetVitalsContext> {
    ApplyVitalsStep() {
        super("apply-vitals");
    }

    @Override
    public StepResult execute(SetVitalsContext context) {
        var mobile = context.getMobile();
        switch (context.getType()) {
            case HITPOINTS -> {
                mobile.setMaxHitpoints(context.getMax());
                mobile.setHitpoints(context.getValue());
            }
            case MANA -> {
                mobile.setMaxMana(context.getMax());
                mobile.setMana(context.getValue());
            }
            case STAMINA -> {
                mobile.setMaxStamina(context.getMax());
                mobile.setStamina(context.getValue());
            }
        }
        return StepResult.success();
    }
}
