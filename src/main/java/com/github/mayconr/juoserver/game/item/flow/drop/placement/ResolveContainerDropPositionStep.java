package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetContainer;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.network.packet.DropItem;

/**
 * Gives a valid position inside the target container to drops that carry none, which happens when the item
 * is dropped onto a mobile or onto a container. The item is placed at the container origin, keeping the grid
 * index; drops that already carry a position are kept as requested.
 */
public final class ResolveContainerDropPositionStep extends AbstractFlowStep<DropItemContext> {
    private static final int ORIGIN = 0;

    public ResolveContainerDropPositionStep() {
        super("resolve-container-drop-position");
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var target = context.getTargetContainer();
        if (hasPosition(target.location())) {
            return StepResult.success();
        }
        context.setTargetContainer(new TargetContainer(target.container(), origin(target.location())));
        return StepResult.success();
    }

    private static boolean hasPosition(Location location) {
        return location.getX() != DropItem.NO_CONTAINER_POSITION
                || location.getY() != DropItem.NO_CONTAINER_POSITION;
    }

    private static Location origin(Location requested) {
        return new PointInTheWorld(ORIGIN, ORIGIN, requested.getZ());
    }
}
