package com.github.mayconr.juoserver.game.ai.definition.animal.flee;

import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.Comparator;

public final class DetectNearbyPlayerStep extends AbstractFlowStep<PassiveAnimalAIContext> {

    private static final int ALERT_RANGE = 8;

    public DetectNearbyPlayerStep() {
        super("DetectNearbyPlayerStep");
    }

    @Override
    public StepResult execute(PassiveAnimalAIContext context) {
        var flee = context.flee();
        var nearest = context.world().mobile()
                .getMobilesInRange(context.npc(), ALERT_RANGE,
                        mobile -> mobile instanceof UOPlayer player && player.isConnected() && player.isAlive())
                .stream()
                .map(UOPlayer.class::cast)
                .min(Comparator.comparingInt(flee::distanceTo))
                .orElse(null);
        flee.updateNearbyPlayer(nearest);
        return StepResult.success();
    }
}
