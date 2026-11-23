package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.GamePlaySettings.AccessLevel;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;

import java.util.HashSet;

final class ValidateContainerOpenAccessStep extends AbstractFlowStep<OpenContainerContext> {
    private final Items items;
    private final AccessLevel accessLevel;

    ValidateContainerOpenAccessStep(Items items, AccessLevel accessLevel) {
        super("validate-container-open-access");
        this.items = items;
        this.accessLevel = accessLevel;
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        var player = context.getPlayer();
        if (accessLevel.isPrivileged(player.getAccessLevel())
                || !(context.getContainer() instanceof UOItem item)) {
            return StepResult.success();
        }

        return canOpen(item, player.getSerialId()) ? StepResult.success()
                : StepResult.failure("CONTAINER_ACCESS_DENIED", "Container does not belong to the player");
    }

    private boolean canOpen(UOItem item, int playerSerial) {
        var visited = new HashSet<Integer>();
        while (item != null && visited.add(item.getSerialId())) {
            switch (item.getCurrentLocation()) {
                case EquippedLocation equipped -> {
                    return Integer.valueOf(playerSerial).equals(equipped.ownerSerialId());
                }
                case ContainerLocation inside -> item = items.get(inside.containerSerialId()).orElse(null);
                case null, default -> {
                    return true;
                }
            }
        }
        return false;
    }
}
