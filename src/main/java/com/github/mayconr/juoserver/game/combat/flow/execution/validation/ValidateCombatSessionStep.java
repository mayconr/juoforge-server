package com.github.mayconr.juoserver.game.combat.flow.execution.validation;

import com.github.mayconr.juoserver.game.combat.CombatParticipants;
import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateCombatSessionStep extends AbstractFlowStep<CombatExecutionContext> {
    public ValidateCombatSessionStep() {
        super("ValidateCombatSession");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        var session = context.getSession();
        if (isSessionUnavailable(session)) {
            return closeSessionAndStop(session);
        }
        return StepResult.success();
    }

    private boolean isSessionUnavailable(CombatSession session) {
        return !session.isActive()
                || isAttackerUnavailable(session)
                || isTargetUnavailable(session);
    }

    private boolean isAttackerUnavailable(CombatSession session) {
        return !CombatParticipants.isAvailable(session.getAttacker());
    }

    private boolean isTargetUnavailable(CombatSession session) {
        return !CombatParticipants.isAvailable(session.getTarget());
    }

    private StepResult closeSessionAndStop(CombatSession session) {
        session.close();
        return StepResult.stop("COMBAT_INACTIVE", "Combat participants or session are unavailable");
    }
}
