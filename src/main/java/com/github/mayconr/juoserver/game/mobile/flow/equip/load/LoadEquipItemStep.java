package com.github.mayconr.juoserver.game.mobile.flow.equip.load;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class LoadEquipItemStep extends AbstractFlowStep<EquipItemContext> {

    private final WorldStorage storage;

    public LoadEquipItemStep(WorldStorage storage) {
        super("load_equip_item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var itemSerialId = context.getItemSerialId();

        if (itemSerialId == null) {
            return StepResult.failure("item serial is null");
        }

        var itemOpt = storage.items().get(itemSerialId);
        if (itemOpt.isEmpty()) {
            return StepResult.failure("item is empty");
        }
        context.setItem(itemOpt.get());

        return StepResult.success();
    }
}
