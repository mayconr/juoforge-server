package com.github.mayconr.juoserver.game.container.flow.add;

import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

/** Refreshes the owning player's status after container placement and notification. */
public final class UpdateContainerOwnerStatusStep extends AbstractFlowStep<AddContainerItemContext> {
    private final RealmStorage storage;
    private final WorldUI ui;

    public UpdateContainerOwnerStatusStep(RealmStorage storage, WorldUI ui) {
        super("update-container-owner-status");
        this.storage = storage;
        this.ui = ui;
    }

    @Override
    public StepResult execute(AddContainerItemContext context) {
        if (context.getContainer().getCurrentLocation() instanceof EquippedLocation location) {
            var owner = storage.getMobile(location.ownerSerialId()).orElse(null);
            if (owner instanceof UOPlayer player) {
                ui.sendStatusGump(player, player.getSerialId());
            }
        }
        return StepResult.success();
    }
}
