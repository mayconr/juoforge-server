package com.github.mayconr.juoserver.game.spell.flow.cast.validation;

import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateCastSpellStep extends AbstractFlowStep<CastSpellContext> {
    public ValidateCastSpellStep() {
        super("ValidateCastSpell");
    }

    @Override
    public StepResult execute(CastSpellContext context) {
        if (context.getCaster() == null) {
            return StepResult.failure("Spell caster is required");
        }
        if (context.getSpellKey() == null || context.getSpellKey().isBlank()) {
            return StepResult.failure("Spell key is required");
        }
        return StepResult.success();
    }
}
