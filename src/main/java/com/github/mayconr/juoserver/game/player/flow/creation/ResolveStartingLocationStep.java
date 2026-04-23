package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveStartingLocationStep extends AbstractFlowStep<PlayerCreationContext> {
    public ResolveStartingLocationStep() {
        super("ResolveStartingLocationStep");
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        int locationIndex = context.getCharacter().getLocationIndex();
        var region = context.getStartingLocations().get(locationIndex);

        if (region == null) {
            return StepResult.failure("Invalid start location index");
        }

        context.setStartingLocation(region);
        return StepResult.success();
    }
}
