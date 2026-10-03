package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.actions.AttackAction;
import com.github.mayconr.juoserver.game.combat.CombatAttackRange;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Requests combat while the target is available and within attack range. */
public final class AttackTargetStep extends AbstractFlowStep<CombatAIContext> {
    public AttackTargetStep() {
        super("AttackTargetStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        if (isSurviving(context) || context.getTarget() == null) return StepResult.skip();
        if (!context.npc().isAlive()) return StepResult.stop();
        if (isTargetUnavailable(context)) {
            context.setTarget(null);
            return StepResult.success();
        }
        if (!isTargetInAttackRange(context)) {
            context.setState(CombatAIState.PURSUING);
            return StepResult.stop();
        }
        context.setState(CombatAIState.ATTACKING);
        context.enqueueAction(new AttackAction(context.npc(), context.getTarget().getSerialId()));
        return StepResult.stop();
    }

    private boolean isSurviving(CombatAIContext context) {
        return context.getState() == CombatAIState.FLEEING || context.getState() == CombatAIState.RECOVERING;
    }

    private boolean isTargetUnavailable(CombatAIContext context) {
        return !context.getTarget().isAlive() || !context.getTarget().isConnected();
    }

    private boolean isTargetInAttackRange(CombatAIContext context) {
        return GameMath.isInRange(context.npc(), context.getTarget(),
                CombatAttackRange.resolve(context.npc(), context.world().mobile()));
    }
}
