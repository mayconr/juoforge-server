package com.github.mayconr.juoserver.game.ai.definition.aggressive.steps;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.ai.actions.AttackAction;
import com.github.mayconr.juoserver.game.combat.CombatAttackRange;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Requests combat while the target is available and within attack range. */
public final class AttackTargetStep extends AbstractFlowStep<AggressiveAIContext> {
    public AttackTargetStep() {
        super("AttackTargetStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
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

    private boolean isSurviving(AggressiveAIContext context) {
        return context.getState() == CombatAIState.FLEEING || context.getState() == CombatAIState.RECOVERING;
    }

    private boolean isTargetUnavailable(AggressiveAIContext context) {
        return !context.getTarget().isAlive() || !context.getTarget().isConnected();
    }

    private boolean isTargetInAttackRange(AggressiveAIContext context) {
        return GameMath.isInRange(context.npc(), context.getTarget(),
                CombatAttackRange.resolve(context.npc(), context.world().mobile()));
    }
}
