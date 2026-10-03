package com.github.mayconr.juoserver.game.ai.definition.aggressive.steps;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.CombatAIState;
import com.github.mayconr.juoserver.game.ai.actions.CancelAttackAction;
import com.github.mayconr.juoserver.game.ai.policy.TargetEligibilityPolicy;
import com.github.mayconr.juoserver.game.ai.policy.TargetSelectionPolicy;
import java.util.Objects;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Retains a valid perceived target, otherwise selects among eligible perceived players. */
public final class SelectTargetStep extends AbstractFlowStep<AggressiveAIContext> {
    private final TargetEligibilityPolicy eligibility;
    private final TargetSelectionPolicy selection;

    public SelectTargetStep() {
        this(TargetEligibilityPolicy.connectedAndAlive(), TargetSelectionPolicy.nearestPlayer());
    }

    public SelectTargetStep(TargetEligibilityPolicy eligibility, TargetSelectionPolicy selection) {
        super("SelectTargetStep");
        this.eligibility = Objects.requireNonNull(eligibility);
        this.selection = Objects.requireNonNull(selection);
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        if (context.getState() == CombatAIState.FLEEING || context.getState() == CombatAIState.RECOVERING) {
            return StepResult.skip();
        }
        var candidates = context.getNearbyPlayers().stream()
                .filter(player -> eligibility.isEligible(context, player)).toList();
        var previous = context.getTarget();
        if (previous != null && eligibility.isEligible(context, previous)) {
            var retained = candidates.stream()
                    .filter(player -> player.getSerialId() == previous.getSerialId()).findFirst();
            if (retained.isPresent()) {
                context.setTarget(retained.get());
                if (context.getState() == CombatAIState.IDLE) context.setState(CombatAIState.PURSUING);
                return StepResult.success();
            }
        }
        var selected = selection.select(context, candidates).orElse(null);
        context.setTarget(selected);
        if (selected != null) {
            if (previous != null && previous.getSerialId() != selected.getSerialId()) {
                context.enqueueAction(new CancelAttackAction(context.npc()));
            }
            context.setState(CombatAIState.PURSUING);
        }
        // With no target, IdleStep owns the state transition and combat cancellation.
        return StepResult.success();
    }
}
