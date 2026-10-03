package com.github.mayconr.juoserver.game.combat.flow.preparation.validation;

import com.github.mayconr.juoserver.game.combat.CombatParticipants;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateCombatParticipantsStep extends AbstractFlowStep<CombatPreparationContext> {
    public ValidateCombatParticipantsStep() { super("ValidateCombatParticipants"); }

    @Override
    public StepResult execute(CombatPreparationContext context) {
        if (!CombatParticipants.isAvailable(context.getAttacker())
                || !CombatParticipants.isAvailable(context.getTargetMobile())
                || context.getAttacker().getSerialId() == context.getTargetMobile().getSerialId()) {
            return StepResult.failure("COMBAT_INVALID_PARTICIPANTS", "Attack requires distinct available participants");
        }
        return StepResult.success();
    }
}
