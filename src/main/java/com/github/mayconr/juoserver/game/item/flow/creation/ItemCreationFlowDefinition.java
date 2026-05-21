package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.item.flow.creation.factory.CreateItemDataStep;
import com.github.mayconr.juoserver.game.item.flow.creation.factory.CreateItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.notification.NotifyItemCreatedStep;
import com.github.mayconr.juoserver.game.item.flow.creation.options.ExecuteOptionsStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceContainerItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceEquippedItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceGroundItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceOrphanItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.template.TemplateLoaderStep;
import com.github.mayconr.juoserver.game.item.flow.creation.validation.ValidateItemCreation;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class ItemCreationFlowDefinition {

    private ItemCreationFlowDefinition() {
    }

    public static Flow<ItemCreationContext> build(GameInfra infra, GameTemplates templates) {
        return FlowFactory.<ItemCreationContext>builder()
                .step(new ValidateItemCreation())
                .step(new ExecuteOptionsStep())
                .step(new TemplateLoaderStep(templates.itemByName(), templates.itemByModelId()))
                .step(new CreateItemDataStep(infra.serialGenerator()))
                .step(new CreateItemStep(infra.storage()))
                .appendGroup("Placement", placementPhase(infra.storage()))
                .step(new NotifyItemCreatedStep(infra.eventBus()))
                .build();
    }

    private static FlowBuilder<ItemCreationContext> placementPhase(RealmStorage storage) {
        return FlowFactory.<ItemCreationContext>builder()
                .step(new PlaceGroundItemStep(storage), ctx-> ctx.getTarget() instanceof GroundItemTarget)
                .step(new PlaceContainerItemStep(storage), ctx-> ctx.getTarget() instanceof ContainerItemTarget)
                .step(new PlaceEquippedItemStep(), ctx-> ctx.getTarget() instanceof EquipItemTarget)
                .step(new PlaceOrphanItemStep(), ctx-> ctx.getTarget() instanceof OrphanItemTarget);
    }

}
