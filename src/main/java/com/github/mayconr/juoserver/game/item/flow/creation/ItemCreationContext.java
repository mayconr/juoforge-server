package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.mobile.shared.ValidateBackpackWeightContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class ItemCreationContext extends AbstractSyncFlowContext<UOItem> implements ValidateBackpackWeightContext {
    private final ItemRequest request;
    private final ItemTarget target;
    private Consumer<ItemCreationOptions> consumerOptions;

    private ItemCreationOptions options;
    private ItemTemplate template;
    private UOItemData data;
    private ItemLocation itemLocation;
    private GameEvent event;
    private UOMobile backpackOwner;
    private int addedWeight;

    @Override
    public UOMobile mobile() {
        return backpackOwner;
    }

    @Override
    public int addedWeight() {
        return addedWeight;
    }

    public ItemCreationContext(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> consumerOptions) {
        this.request = request;
        this.target = target;
        this.consumerOptions = consumerOptions;
    }

}
