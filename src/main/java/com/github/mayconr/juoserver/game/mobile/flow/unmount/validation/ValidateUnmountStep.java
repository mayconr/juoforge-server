package com.github.mayconr.juoserver.game.mobile.flow.unmount.validation;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ValidateUnmountStep extends AbstractFlowStep<UnmountContext> {
    public ValidateUnmountStep() {
        super("ValidateUnmount");
    }

    @Override
    public StepResult execute(UnmountContext context) {
        if (context.getMobile() == null) {
            return StepResult.failure("Mobile is null");
        }
        if (!context.getMobile().getEquippedItems().containsKey(Layer.MOUNT)) {
            return StepResult.failure("Mobile is not mounted");
        }

        return StepResult.success();
    }
}
