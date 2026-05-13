package com.github.mayconr.juoserver.game.mobile.flow.unmount.load;

import com.github.mayconr.juoserver.game.mobile.flow.unmount.UnmountContext;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class LoadMountItemStep extends AbstractFlowStep<UnmountContext> {

    private final RealmStorage storage;

    public LoadMountItemStep(RealmStorage storage) {
        super("LoadMountItem");
        this.storage = storage;
    }

    @Override
    public StepResult execute(UnmountContext context) {
        final var mobile = context.getMobile();

        final var itemSerial = mobile.getEquippedItems().get(Layer.MOUNT);
        if (itemSerial == null) {
            return StepResult.failure("Mobile is not mounted");
        }

        var item = storage.getItem(itemSerial).orElse(null);
        if (item == null) {
            return StepResult.failure("Item not found for serial " + itemSerial);
        }

        context.setMountItem(item);

        return StepResult.success();
    }
}
