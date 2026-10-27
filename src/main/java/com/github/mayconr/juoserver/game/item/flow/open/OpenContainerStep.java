package com.github.mayconr.juoserver.game.item.flow.open;

import com.github.mayconr.juoserver.game.loot.WorldLoot;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.event.ContainerOpenedEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenContainerStep extends AbstractFlowStep<OpenContainerContext> {
    private final RealmStorage storage;
    private final EventBus eventBus;
    private final WorldLoot loot;

    public OpenContainerStep(RealmStorage storage, EventBus eventBus, WorldLoot loot) {
        super("open-container");
        this.storage = storage;
        this.eventBus = eventBus;
        this.loot = loot;
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        var container = context.getContainer();
        storage.loadContainerItems(container).thenAccept(items -> {
            synchronized (container) {
                container.addItemsToContainer(items);
                if (container instanceof UOCorpse corpse) {
                    try {
                        loot.materialize(corpse);
                    } catch (RuntimeException failure) {
                        log.error("Cannot materialize loot for corpse {}", corpse.getSerialId(), failure);
                    }
                }
                eventBus.publish(new ContainerOpenedEvent(context.getPlayer(), container));
            }
        });
        return StepResult.success();
    }
}
