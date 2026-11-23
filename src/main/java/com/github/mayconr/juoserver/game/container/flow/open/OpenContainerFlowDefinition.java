package com.github.mayconr.juoserver.game.container.flow.open;

import com.github.mayconr.juoserver.game.GamePlaySettings.AccessLevel;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.loot.WorldLoot;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public final class OpenContainerFlowDefinition {
    private OpenContainerFlowDefinition() { }

    public static Flow<OpenContainerContext> build(
            WorldStorage storage, EventBus eventBus, WorldLoot loot, ItemModule items, AccessLevel accessLevel
    ) {
        return FlowFactory.<OpenContainerContext>builder()
                .step(new ValidateContainerOpenAccessStep(storage.items(), accessLevel))
                .step(new EquipContainerBeforeOpeningStep(items), OpenContainerContext::isEquipBeforeOpening)
                .step(new ValidateContainerEquippedStep(), OpenContainerContext::isEquipBeforeOpening)
                .step(new LoadContainerItemsStep(storage.items()))
                .step(new OpenContainerStep(eventBus, loot))
                .build();
    }
}
