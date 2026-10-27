package com.github.mayconr.juoserver.game.ui.flow.doubleclick;

import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class UseClickedItemStep extends AbstractFlowStep<DoubleClickContext> {
    private final RealmStorage storage;
    private final WorldItem items;

    public UseClickedItemStep(RealmStorage storage, WorldItem items) {
        super("use-clicked-item");
        this.storage = storage;
        this.items = items;
    }

    @Override
    public StepResult execute(DoubleClickContext context) {
        storage.getItem(context.getRequest().getSerialId()).ifPresent(item -> {
            if (item instanceof Container container) {
                items.openContainer(context.getPlayer(), container);
            } else {
                items.use(new ItemUseContext(context.getPlayer(), item, Trigger.DOUBLE_CLICK));
            }
        });
        return StepResult.success();
    }
}
