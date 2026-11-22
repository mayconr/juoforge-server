package com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.combat.CombatAttackRange;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Calculates the direct direction toward a target outside attack range. */
public final class PursueTargetStep extends AbstractFlowStep<AggressiveAIContext> {
    public PursueTargetStep() {
        super("PursueTargetStep");
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        var pursuit = context.getPursuit();
        pursuit.setNextDirection(null);
        pursuit.setMovementBlocked(false);

        if (isSurviving(context)) {
            return StepResult.skip();
        }
        if (isWithoutTarget(context)) {
            return StepResult.skip();
        }
        if (isTargetUnavailable(context.getTarget())) {
            context.setTarget(null);
            return StepResult.success();
        }
        if (isTargetInAttackRange(context)) {
            context.setState(CombatAIState.ATTACKING);
            return StepResult.success();
        }

        context.setState(CombatAIState.PURSUING);
        pursuit.setNextDirection(directionTowardTarget(context.npc(), context.getTarget()));
        return StepResult.success();
    }

    private boolean isSurviving(AggressiveAIContext context) {
        return context.getState() == CombatAIState.FLEEING
                || context.getState() == CombatAIState.RECOVERING;
    }

    private boolean isWithoutTarget(AggressiveAIContext context) {
        return context.getTarget() == null;
    }

    private boolean isTargetUnavailable(UOPlayer target) {
        return !target.isConnected() || !target.isAlive();
    }

    private boolean isTargetInAttackRange(AggressiveAIContext context) {
        return GameMath.isInRange(context.npc(), context.getTarget(),
                CombatAttackRange.resolve(context.npc(), context.world().mobile()));
    }

    private Direction directionTowardTarget(UONpc npc, UOPlayer target) {
        int dx = Integer.compare(target.getX(), npc.getX());
        int dy = Integer.compare(target.getY(), npc.getY());
        return dx == 0 && dy == 0 ? null : Direction.fromDelta(dx, dy);
    }
}
