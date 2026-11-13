package com.github.mayconr.juoserver.game.item.flow.drop.validation;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.CarriedBy;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.OnGround;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.Unreachable;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRootResolver;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Checks the destination's world location and carrier without changing the dropped item. */
public final class ValidateInteractionRangeStep extends AbstractFlowStep<DropItemContext> {
    private final ItemRootResolver roots;

    public ValidateInteractionRangeStep(ItemRootResolver roots) {
        super("validate-interaction-range");
        this.roots = roots;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        if (context.isGroundDrop()) {
            return validateRange(context, context.getTargetGround().location());
        }
        var target = context.isContainerDrop()
                ? context.getTargetContainer().container() : context.getTargetStack().itemStack();
        return switch (roots.resolve(target)) {
            case OnGround ground -> validateRange(context, ground.location());
            case CarriedBy carrier -> carrier.mobile().getSerialId() == context.getPlayer().getSerialId()
                    ? StepResult.success()
                    : StepResult.failure("DROP_ACCESS_DENIED", "Destination is carried by another mobile");
            case Unreachable unreachable -> StepResult.failure("DROP_UNREACHABLE", "Destination is not in the world");
        };
    }

    private static StepResult validateRange(DropItemContext context, Location destination) {
        return GameMath.isInRange(context.getPlayer(), destination, 2)
                ? StepResult.success()
                : StepResult.failure("DROP_OUT_OF_RANGE", "Destination is out of reach");
    }
}
