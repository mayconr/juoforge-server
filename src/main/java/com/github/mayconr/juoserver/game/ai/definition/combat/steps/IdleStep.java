package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.actions.CancelAttackAction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Stands still without a target, cancelling combat once when entering idle. */
public final class IdleStep extends AbstractFlowStep<CombatAIContext> {
    public IdleStep() {
        super("IdleStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        var state = context.getState();
        if (state == CombatAIState.FLEEING || state == CombatAIState.RECOVERING
                || context.getTarget() != null) {
            return StepResult.skip();
        }
        if (state != CombatAIState.IDLE) {
            context.enqueueAction(new CancelAttackAction(context.npc()));
            context.setState(CombatAIState.IDLE);
        }
        return StepResult.stop();
    }
}
