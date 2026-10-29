package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

/** Resolves the equipped container owner for the shared status notification step. */
public final class FindContainerOwnerStep extends AbstractFlowStep<AddContainerItemContext> {
    private final RealmStorage storage;

    public FindContainerOwnerStep(RealmStorage storage) {
        super("find-container-owner");
        this.storage = storage;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        var owner = context.getContainer().getCurrentLocation() instanceof EquippedLocation location
                ? storage.getMobile(location.ownerSerialId()).orElse(null) : null;
        context.setMobile(owner);
        return StepResult.success();
    }
}
