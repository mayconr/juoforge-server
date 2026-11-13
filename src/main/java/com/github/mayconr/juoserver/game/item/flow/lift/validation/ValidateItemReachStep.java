package com.github.mayconr.juoserver.game.item.flow.lift.validation;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.CarriedBy;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.OnGround;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRoot.Unreachable;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/**
 * The player can lift items carried by itself, at any depth inside its equipment, and items whose outermost
 * container lies on the ground within {@link #LIFT_RANGE} tiles. Items carried by any other mobile are refused.
 */
public final class ValidateItemReachStep extends AbstractFlowStep<LiftItemContext> {
    public static final int LIFT_RANGE = 2;

    private final ItemRootResolver roots;

    public ValidateItemReachStep(ItemRootResolver roots) {
        super("validate-item-reach");
        this.roots = roots;
    }

    @Override
    public StepResult execute(LiftItemContext context) {
        return switch (roots.resolve(context.getItem())) {
            case OnGround ground -> validateGround(context, ground);
            case CarriedBy carrier -> validateCarrier(context, carrier);
            case Unreachable unreachable -> context.reject(LiftRejectReason.CANNOT_LIFT,
                    "Item " + context.getItem().getSerialId() + " is not placed in the world");
        };
    }

    private static StepResult validateGround(LiftItemContext context, OnGround ground) {
        if (GameMath.isInRange(context.getPlayer(), ground.location(), LIFT_RANGE)) {
            return StepResult.success();
        }
        return context.reject(LiftRejectReason.OUT_OF_RANGE, "Item is out of the player's reach");
    }

    private static StepResult validateCarrier(LiftItemContext context, CarriedBy carrier) {
        if (carrier.mobile().getSerialId() == context.getPlayer().getSerialId()) {
            return StepResult.success();
        }
        return context.reject(LiftRejectReason.TRY_TO_STEAL,
                "Item is carried by mobile " + carrier.mobile().getSerialId());
    }
}
