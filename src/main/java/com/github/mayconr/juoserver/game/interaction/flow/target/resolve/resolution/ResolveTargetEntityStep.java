package com.github.mayconr.juoserver.game.interaction.flow.target.resolve.resolution;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.HashSet;

public class ResolveTargetEntityStep extends AbstractFlowStep<ResolveTargetContext> {

    private final WorldStorage storage;

    public ResolveTargetEntityStep(WorldStorage storage) {
        super("BuildTargetResult");
        this.storage = storage;
    }

    @Override
    public StepResult execute(ResolveTargetContext context) {
        if (CursorTarget.LOCATION.equals(context.getCursorTarget())) {
            return StepResult.success();
        }

        int serial = context.getSerialId();

        if (UOMobile.isMobile(serial)) {
            return storage.mobiles().get(serial)
                    .map(m -> {
                        context.setMobile(m);
                        return StepResult.success();
                    })
                    .orElse(StepResult.failure("Mobile not found"));
        }

        if (UOItem.isItem(serial)) {
            return storage.items().get(serial)
                    .map(i -> {
                        context.setItem(i);
                        resolveItemLocation(context, i);
                        return StepResult.success();
                    })
                    .orElse(StepResult.failure("Item not found"));
        }

        return StepResult.failure("Unknown cursor type");
    }

    private void resolveItemLocation(ResolveTargetContext context, UOItem item) {
        if (item.isOnTheGround()) {
            context.setTargetLocation(item);
        } else if (isInOwnBackpack(context, item)) {
            context.setInOwnBackpack(true);
            context.setTargetLocation(context.getPlayer());
        }
    }

    private boolean isInOwnBackpack(ResolveTargetContext context, UOItem item) {
        var visited = new HashSet<Integer>();
        var current = item;
        while (current != null && visited.add(current.getSerialId())) {
            if (current.getCurrentLocation() instanceof EquippedLocation equipped) {
                return current.getLayer() == Layer.BACKPACK
                        && equipped.ownerSerialId() != null
                        && equipped.ownerSerialId() == context.getPlayer().getSerialId();
            }
            if (!(current.getCurrentLocation() instanceof ContainerLocation container)
                    || container.containerSerialId() == null) {
                return false;
            }
            current = storage.items().get(container.containerSerialId()).orElse(null);
        }
        return false;
    }
}
