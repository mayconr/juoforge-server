package com.github.mayconr.juoserver.game.mobile.flow.equip.state;

import com.github.mayconr.juoserver.game.item.ItemPlacement;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class CleanupItemStateStep extends AbstractFlowStep<EquipItemContext> {
    private final WorldStorage storage;
    private final WorldMobile mobiles;

    public CleanupItemStateStep(WorldStorage storage, WorldMobile mobiles) {
        super("cleanup_item_state");
        this.storage = storage;
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var item = context.getItem();

        ItemPlacement.detach(item, storage, mobiles, false);

        return StepResult.success();
    }
}
