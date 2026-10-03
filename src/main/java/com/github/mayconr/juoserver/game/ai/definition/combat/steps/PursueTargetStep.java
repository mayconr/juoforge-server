package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIState;
import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.combat.CombatAttackRange;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Requests one movement per AI update until the target is within attack range. */
public final class PursueTargetStep extends AbstractFlowStep<CombatAIContext> {
    public PursueTargetStep() {
        super("PursueTargetStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
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
        enqueueMovementTowardTarget(context);
        return StepResult.stop();
    }

    private boolean isSurviving(CombatAIContext context) {
        return context.getState() == CombatAIState.FLEEING
                || context.getState() == CombatAIState.RECOVERING;
    }

    private boolean isWithoutTarget(CombatAIContext context) {
        return context.getTarget() == null;
    }

    private boolean isTargetUnavailable(UOPlayer target) {
        return !target.isConnected() || !target.isAlive();
    }

    private boolean isTargetInAttackRange(CombatAIContext context) {
        return GameMath.isInRange(context.npc(), context.getTarget(),
                CombatAttackRange.resolve(context.npc(), context.world().mobile()));
    }

    private void enqueueMovementTowardTarget(CombatAIContext context) {
        var npc = context.npc();
        var target = context.getTarget();
        // Different elevation on the same tile needs navigation, not a horizontal direction.
        if (isAtSameHorizontalPosition(npc, target)) {
            return;
        }
        int dx = Integer.compare(target.getX(), npc.getX());
        int dy = Integer.compare(target.getY(), npc.getY());
        context.enqueueAction(new WalkAction(npc, Direction.fromDelta(dx, dy)));
    }

    private boolean isAtSameHorizontalPosition(UONpc npc, UOPlayer target) {
        return npc.getX() == target.getX() && npc.getY() == target.getY();
    }
}
