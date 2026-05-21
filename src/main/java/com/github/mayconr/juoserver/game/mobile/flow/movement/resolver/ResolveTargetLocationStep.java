package com.github.mayconr.juoserver.game.mobile.flow.movement.resolver;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveTargetLocationStep extends AbstractFlowStep<MovementContext> {
    public ResolveTargetLocationStep() {
        super("TargetResolver");
    }

    @Override
    public StepResult execute(MovementContext context) {
        final var mobile = context.getMobile();
        final var direction = context.getDirection();

        Location target;
        if (mobile.getDirection().equals(direction)) {
            target = new PointInTheWorld(mobile.getX() + direction.getDx(), mobile.getY() + direction.getDy(), mobile.getZ());
        } else {
            target = mobile;
        }

        context.setTargetLocation(target);

        return StepResult.success();
    }
}
