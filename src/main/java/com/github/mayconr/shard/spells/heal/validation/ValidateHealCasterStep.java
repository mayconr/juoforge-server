package com.github.mayconr.shard.spells.heal.validation;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.shard.spells.heal.HealContext;

public class ValidateHealCasterStep extends AbstractFlowStep<HealContext> {
    public ValidateHealCasterStep() {
        super("ValidateHealCaster");
    }

    @Override
    public StepResult execute(HealContext context) {
        return context.getCaster() == null
                ? StepResult.failure("Heal caster is required")
                : StepResult.success();
    }
}
