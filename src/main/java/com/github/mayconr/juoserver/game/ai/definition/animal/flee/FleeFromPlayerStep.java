package com.github.mayconr.juoserver.game.ai.definition.animal.flee;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class FleeFromPlayerStep extends AbstractFlowStep<PassiveAnimalAIContext> {

    public FleeFromPlayerStep() {
        super("FleeFromPlayerStep");
    }

    @Override
    public StepResult execute(PassiveAnimalAIContext context) {
        var npc = context.npc();
        var player = context.flee().nearbyPlayer();
        int dx = npc.getX() - player.getX();
        int dy = npc.getY() - player.getY();
        var direction = dx == 0 && dy == 0 ? Direction.SOUTH : Direction.fromDelta(dx, dy);
        context.enqueueAction(new WalkAction(npc, direction));
        return StepResult.success();
    }
}
