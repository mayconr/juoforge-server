package com.github.mayconr.juoserver.game.mobile.flow.teleport.resolver;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveDirectionStep extends AbstractFlowStep<TeleportContext> {
    public ResolveDirectionStep() {
        super("ResolveDirection");
    }

    @Override
    public StepResult execute(TeleportContext context) {
        final var from = context.getMobile();
        final var to = context.getLocation();

        final int dx = to.getX() - from.getX();
        final int dy = to.getY() - from.getY();

        if (dx == 0 && dy == 0) {
            return StepResult.failure("You need to specify at least one direction");
        }

        context.setDirection(Direction.fromDelta(dx, dy));

        return StepResult.success();
    }
}
