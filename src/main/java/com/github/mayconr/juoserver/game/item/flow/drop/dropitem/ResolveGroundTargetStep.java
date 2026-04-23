package com.github.mayconr.juoserver.game.item.flow.drop.dropitem;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext.TargetGround;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveGroundTargetStep extends AbstractFlowStep<DropItemContext> {
    public ResolveGroundTargetStep() {
        super("resolve_ground_target");
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var dropItem = context.getDropItem();

        var location = new PointInTheWorld(
                dropItem.getX(),
                dropItem.getY(),
                dropItem.getZ()
        );

        context.setTargetGround(new TargetGround(location));
        return StepResult.success();
    }
}
