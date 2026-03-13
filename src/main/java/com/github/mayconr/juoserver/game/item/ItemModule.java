package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ItemModule implements WorldModule, ItemCommands, ItemQueries {

    private final ItemHandler itemHandler;
    private final ItemDropHandler itemDropHandler;
    private final ContainerHandler containerHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public UOItem createItemAtLocation(ItemCreationRequest request, Location location) {
        return itemHandler.createItemAtLocation(request, location);
    }

    @Override
    public UOItem createEquippedItem(ItemCreationRequest request, UOMobile mobile) {
        return itemHandler.createEquippedItem(request, mobile);
    }

    @Override
    public UOItem createItemInContainer(ItemCreationRequest request, Container container) {
        return itemHandler.createItemInContainer(request, container);
    }

    @Override
    public UOItem createUnloadedItem(ItemCreationRequest request) {
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
        itemDropHandler.dropItemOnTheGround(player, droppedItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, DropItem dropItem) {
        itemDropHandler.dropItemInContainer(player, dropItem);
    }

    @Override
    public void dropItemInContainer(UOPlayer player, UOItem item, Container container) {
        itemDropHandler.dropItemInContainer(player, item, container);
    }

    @Override
    public List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> predicate) {
        return containerHandler.getItemsInContainer(container, predicate);
    }

    @Override
    public ConsumeResult consumeItem(Container container, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(container, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }
}
