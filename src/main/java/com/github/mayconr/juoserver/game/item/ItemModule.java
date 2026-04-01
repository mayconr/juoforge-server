package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ItemModule implements WorldModule, ItemCommands, ItemQueries {

    private final ItemHandler itemHandler;
    private final ItemDropService itemDropService;
    private final ContainerHandler containerHandler;

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target) {
        return itemHandler.createItem(request, target, opt->{});
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
        return itemHandler.createItem(request, target, options);
    }

    @Override
    public UOItem createUnloadedItem(ItemRequest request) {
        return itemHandler.createUnloadedItem(request);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemHandler.deleteItem(item);
    }

    @Override
    public void moveItem(UOItem item, Location location) {
        itemHandler.moveItem(item, location);
    }

    @Override
    public void dropItemOnTheGround(UOPlayer player, DropItem droppedItem) {
        itemDropService.dropItemOnTheGround(player, droppedItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, DropItem dropItem) {
        itemDropService.dropItemInContainer(player, dropItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, UOItem item, Container container) {
        itemDropService.dropItemInContainer(player, item, container);
    }

    @Override
    public List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> predicate) {
        return containerHandler.getItemsInContainer(container, predicate);
    }

    @Override
    public ConsumeResult consumeItem(UOContainer container, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(container, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }
}
