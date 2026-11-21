package com.github.mayconr.juoserver.game.item.flow.drop.placement;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class TransformToOrphanStep extends AbstractFlowStep<DropItemContext> {

    private final WorldStorage storage;

    private final WorldMobile mobiles;

    public TransformToOrphanStep(WorldStorage storage, WorldMobile mobiles) {
        super("transform_to_orphan");
        this.storage = storage;
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(DropItemContext context) {
        var item = context.getItem();

        ItemPlacement.detach(item, storage, mobiles, true);

        return StepResult.success();
    }
}
