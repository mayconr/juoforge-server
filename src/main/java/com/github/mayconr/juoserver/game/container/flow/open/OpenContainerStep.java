package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.loot.WorldLoot;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.model.event.ContainerOpenedEvent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenContainerStep extends AbstractFlowStep<OpenContainerContext> {
    private final EventBus eventBus;
    private final WorldLoot loot;

    public OpenContainerStep(EventBus eventBus, WorldLoot loot) {
        super("open-container");
        this.eventBus = eventBus;
        this.loot = loot;
    }

    @Override
    public StepResult execute(OpenContainerContext context) {
        var container = context.getContainer();
        context.getContents().thenAccept(items -> {
            synchronized (container) {
                container.addItemsToContainer(items);
                if (container instanceof UOCorpse corpse) {
                    try {
                        loot.materialize(corpse);
                    } catch (RuntimeException failure) {
                        log.error("Cannot materialize loot for corpse {}", corpse.getSerialId(), failure);
                    }
                }
                eventBus.publish(new ContainerOpenedEvent(
                        context.getPlayer(), container, context.isEquipBeforeOpening()));
            }
        });
        return StepResult.success();
    }
}
