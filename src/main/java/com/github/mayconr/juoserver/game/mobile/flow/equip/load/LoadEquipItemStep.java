package com.github.mayconr.juoserver.game.mobile.flow.equip.load;

import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadEquipItemStep extends AbstractFlowStep<EquipItemContext> {

    private final RealmStorage storage;

    public LoadEquipItemStep(RealmStorage storage) {
        super("load_equip_item");
        this.storage = storage;
    }

    @Override
    public StepResult execute(EquipItemContext context) {
        final var equipItem = context.getEquipItem();

        if (equipItem == null) {
            return StepResult.failure("item is null");
        }

        var itemOpt = storage.getItem(equipItem.getItemSerialId());
        if (itemOpt.isEmpty()) {
            return StepResult.failure("item is empty");
        }
        context.setItem(itemOpt.get());

        return StepResult.success();
    }
}
