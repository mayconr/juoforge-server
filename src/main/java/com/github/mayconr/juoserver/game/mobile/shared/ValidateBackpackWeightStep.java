package com.github.mayconr.juoserver.game.mobile.shared;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.RequiredArgsConstructor;

/** Validates the supplied additional weight without mutating the backpack. */
@RequiredArgsConstructor
public final class ValidateBackpackWeightStep<T extends ValidateBackpackWeightContext> implements FlowStep<T> {
    private final WorldMobile mobiles;
    private final WorldContainer containers;

    @Override
    public String name() {
        return "validate-backpack-weight";
    }

    @Override
    public StepResult execute(T context) {
        var mobile = context.mobile();
        var backpackSerial = mobile.getBackpack();
        if (backpackSerial == null) {
            return StepResult.failure("BACKPACK_NOT_FOUND", "Mobile has no backpack");
        }
        var backpack = containers.getContainerBySerialId(backpackSerial).orElse(null);
        if (backpack == null) {
            return StepResult.failure("BACKPACK_NOT_FOUND", "Backpack is not loaded: " + backpackSerial);
        }
        long currentWeight = containers.getWeight(backpack);
        int addedWeight = context.addedWeight();
        long estimatedWeight = Math.addExact(currentWeight, addedWeight);
        int maxWeight = mobiles.calculateMaxWeight(mobile);
        if (estimatedWeight > maxWeight) {
            return StepResult.failure("BACKPACK_OVERWEIGHT",
                    "Estimated backpack weight " + estimatedWeight + " exceeds maximum " + maxWeight);
        }
        return StepResult.success();
    }
}
