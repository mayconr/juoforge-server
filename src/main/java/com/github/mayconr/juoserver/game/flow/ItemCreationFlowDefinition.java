package com.github.mayconr.juoserver.game.flow;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.flow.creation.*;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceContainerItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceEquippedItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceGroundItemStep;
import com.github.mayconr.juoserver.game.item.flow.creation.placement.PlaceOrphanItemStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

public final class ItemCreationFlowDefinition {

    private ItemCreationFlowDefinition() {
    }

    public static Flow<ItemCreationContext> build(TemplateRegistry<String, ItemTemplate> itemTemplateByName,
                                                  TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId,
                                                  SerialGenerator serialGenerator,
                                                  RealmStorage storage,
                                                  EventBus eventBus) {
        return FlowFactory.<ItemCreationContext>builder()
                .step(new ValidateItemCreation())
                .step(new ExecuteOptionsStep())
                .step(new TemplateLoaderStep(itemTemplateByName, itemTemplateByModelId))
                .step(new CreateItemDataStep(serialGenerator))
                .step(new CreateItemStep(storage))
                .step(new IdentifyLocationTypeStep())
                .appendGroup("Placemento", placementPhase(storage))
                .step(new NotifyItemCreatedStep(eventBus))
                .build();
    }

    private static FlowBuilder<ItemCreationContext> placementPhase(RealmStorage storage) {
        return FlowFactory.<ItemCreationContext>builder()
                .step(new PlaceGroundItemStep(storage), ctx-> ctx.itemLocation instanceof GroundLocation)
                .step(new PlaceContainerItemStep(storage), ctx-> ctx.itemLocation instanceof ContainerLocation)
                .step(new PlaceEquippedItemStep(), ctx-> ctx.itemLocation instanceof EquippedLocation)
                .step(new PlaceOrphanItemStep(), ctx-> ctx.itemLocation instanceof OrphanLocation);
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @RequiredArgsConstructor
    public static class ItemCreationContext extends SyncFlowContext<UOItem> {
        private final ItemRequest request;
        private final ItemTarget target;
        private Consumer<ItemCreationOptions> consumerOptions;

        private ItemCreationOptions options;
        private ItemTemplate template;
        private UOItemData data;
        private ItemLocation itemLocation;
        private GameEvent event;

        public ItemCreationContext(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> consumerOptions) {
            this.request = request;
            this.target = target;
            this.consumerOptions = consumerOptions;
        }

    }
}
