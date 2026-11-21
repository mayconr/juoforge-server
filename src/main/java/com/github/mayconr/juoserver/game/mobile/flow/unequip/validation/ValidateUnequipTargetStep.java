package com.github.mayconr.juoserver.game.mobile.flow.unequip.validation;

import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.model.GroundItemTarget;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.model.OrphanItemTarget;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Validates the supported destinations before changing equipment. */
public final class ValidateUnequipTargetStep extends AbstractFlowStep<UnequipItemContext> {
    public ValidateUnequipTargetStep() {
        super("validate-unequip-target");
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        return switch (context.getTarget()) {
            case OrphanItemTarget target -> StepResult.success();
            case GroundItemTarget target when target.location() != null ->
                    ValidateInteractionRangeStep.validateRange(context.getMobile(), target.location());
            case null, default -> StepResult.failure("UNEQUIP_INVALID_TARGET",
                    "Unequip requires an orphan target or a ground target with a location");
        };
    }
}
