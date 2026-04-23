package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.flow.DropItemFlowDefinition.DropItemContext;
import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ItemModuleImpl implements ItemModule {

    private final ItemHandler itemHandler;
    private final ContainerHandler containerHandler;
    private FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target) {
        var context = new ItemCreationContext(request, target);
        flows.execute(context);
        return context.result();
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
        var context = new ItemCreationContext(request, target, options);
        flows.execute(context);
        return context.result();
    }

    @Override
    public void deleteItem(UOItem item) {
        itemHandler.deleteItem(item);
    }

    @Override
    public void dropItem(UOPlayer player, DropItem dropItem) {
        flows.execute(DropItemContext.ofDropItem(player, dropItem));
    }

    @Override
    public List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate) {
        return containerHandler.getItemsInContainer(containerSerial, predicate);
    }

    @Override
    public ConsumeResult consumeItem(Integer containerSerial, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(containerSerial, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }
}
