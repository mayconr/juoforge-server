package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class TransformToOrphanStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;

    public TransformToOrphanStep(WorldStorage storage) {
        super("transform_to_orphan");
        this.storage = storage;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var item = context.getItem();

        // Item was equipped
        switch (item.getCurrentLocation()) {
            case EquippedLocation location -> {
                var owner = storage.mobiles().get(location.ownerSerialId())
                        .orElseThrow(() -> new IllegalStateException("Owner not found"));
                owner.getEquippedItems().remove(item.getLayer());
            }
            case OrphanLocation l -> {}
            case GroundLocation l -> storage.items().removeFromGround(item);
            case ContainerLocation location -> {
                var container = storage.items().getContainer(location.containerSerialId())
                        .orElseThrow(() -> new IllegalStateException("Container not found"));
                container.removeItemFromContainer(item);
            }
        }

        item.setCurrentLocation(new OrphanLocation());

        return StepResult.success();
    }
}
