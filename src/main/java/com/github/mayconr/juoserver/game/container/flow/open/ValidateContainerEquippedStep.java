package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.Objects;

/** Aborts the opening when the container is equipped on a mobile other than the player who requested it. */
final class ValidateContainerEquippedStep extends AbstractFlowStep<OpenContainerContext> {
    ValidateContainerEquippedStep() {
        super("validate-container-equipped");
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        var player = context.getPlayer();
        synchronized (player) {
            if (context.getContainer() instanceof UOItem item && isEquippedOnAnother(player, item)) {
                return StepResult.failure("CONTAINER_EQUIPPED_ON_ANOTHER",
                        "Container is equipped on another mobile than the requesting player");
            }
        }
        return StepResult.success();
    }

    private boolean isEquippedOnAnother(UOPlayer player, UOItem item) {
        return item.getCurrentLocation() instanceof EquippedLocation equipped
                && !Objects.equals(equipped.ownerSerialId(), player.getSerialId());
    }
}
