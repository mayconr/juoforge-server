package com.github.mayconr.juoserver.game.ui.flow.status;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class CalculateStatusWeightStep extends AbstractFlowStep<StatusGumpContext> {
    private final RealmStorage storage;
    private final WorldContainer containers;

    public CalculateStatusWeightStep(RealmStorage storage, WorldContainer containers) {
        super("calculate-status-weight");
        this.storage = storage;
        this.containers = containers;
    }

    @Override
    public StepResult execute(StatusGumpContext context) {
        var mobile = context.getMobile();
        int weight = 0;
        if (mobile.getBackpack() != null) {
            var backpack = storage.getContainer(mobile.getBackpack()).orElse(null);
            if (backpack == null) {
                return StepResult.failure("BACKPACK_NOT_FOUND",
                        "Cannot calculate status weight: backpack " + mobile.getBackpack() + " is not loaded");
            }
            weight = Math.toIntExact(containers.getWeight(backpack));
        }
        context.setWeight(weight);
        return StepResult.success();
    }
}
