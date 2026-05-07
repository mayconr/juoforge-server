package com.github.mayconr.juoserver.game.mobile.flow.unequip.load;

import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadUnequipItemStep extends AbstractFlowStep<UnequipItemContext> {

    private final RealmStorage storage;

    public LoadUnequipItemStep(RealmStorage storage) {
        super("load_unequip_item_step");
        this.storage = storage;
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        var unequipItem = context.getUnequipItem();
        if (unequipItem == null) {
            return StepResult.failure("UnequipItem is null");
        }

        var itemOpt = storage.getItem(unequipItem.getSerialId());
        if (itemOpt.isEmpty()) {
            return StepResult.failure("Item was not found for serial " + unequipItem.getSerialId());
        }
        context.setItem(itemOpt.get());

        return StepResult.success();
    }
}
