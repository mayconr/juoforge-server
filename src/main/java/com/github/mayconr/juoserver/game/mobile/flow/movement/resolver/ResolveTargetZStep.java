package com.github.mayconr.juoserver.game.mobile.flow.movement.resolver;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext.MoveIntent;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * Resolves the Z of the destination tile from the map data, rejecting steps the mobile cannot walk.
 */
public class ResolveTargetZStep extends AbstractFlowStep<MovementContext> {

    private final UOFileReader fileReader;

    public ResolveTargetZStep(UOFileReader fileReader) {
        super("ResolveTargetZ");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(MovementContext context) {
        if (MoveIntent.TURN.equals(context.getMoveIntent())) {
            return StepResult.success();
        }

        final var walkZ = fileReader.resolveWalkZ(context.getMobile(), context.getDirection());
        if (walkZ.isEmpty()) {
            return StepResult.failure("Target location is not walkable");
        }

        final var target = context.getTargetLocation();
        context.setTargetLocation(new PointInTheWorld(target.getX(), target.getY(), walkZ.getAsInt()));

        return StepResult.success();
    }
}
