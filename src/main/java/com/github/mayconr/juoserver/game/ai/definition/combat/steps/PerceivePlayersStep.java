package com.github.mayconr.juoserver.game.ai.definition.combat.steps;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.model.BehaviorDefinition;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.List;

/** Refreshes nearby connected, living players using the world's spatial index. */
public final class PerceivePlayersStep extends AbstractFlowStep<CombatAIContext> {
    public PerceivePlayersStep() {
        super("PerceivePlayersStep");
    }

    @Override
    public StepResult execute(CombatAIContext context) {
        var behavior = context.npc().getBehavior();
        if (isPerceptionRadiusMissing(behavior)) {
            return clearPerceptionAndFail(context);
        }

        context.setNearbyPlayers(findNearbyPlayers(context, behavior.perceptionRadius()));
        return StepResult.success();
    }

    private boolean isPerceptionRadiusMissing(BehaviorDefinition behavior) {
        return behavior == null || behavior.perceptionRadius() == null;
    }

    private StepResult clearPerceptionAndFail(CombatAIContext context) {
        context.setNearbyPlayers(List.of());
        return StepResult.failure("AI_PERCEPTION_RADIUS_MISSING", "Combat AI requires perceptionRadius");
    }

    private List<UOPlayer> findNearbyPlayers(CombatAIContext context, int perceptionRadius) {
        return context.world().storage()
                .getMobilesInRange(context.npc(), perceptionRadius, this::isPerceivablePlayer)
                .stream()
                .map(UOPlayer.class::cast)
                .toList();
    }

    private boolean isPerceivablePlayer(UOMobile mobile) {
        return mobile instanceof UOPlayer player
                && player.isConnected()
                && player.isAlive();
    }
}
