package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemModule implements WorldModule, ItemCommands {

    private final ItemHandler itemHandler;
    private final ItemDropHandler itemDropHandler;
    private final ItemEquipHandler itemEquipHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public UOItem createItemAtLocation(String name, Location location) {
        return itemHandler.createItemAtLocation(name, location);
    }

    @Override
    public UOItem createEquippedItem(UOMobile mobile, String name) {
        return itemHandler.createEquippedItem(mobile, name);
    }

    @Override
    public UOItem createContainerItem(String name, Container container) {
        return itemHandler.createContainerItem(name, container);
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
    public void equipItem(UOPlayer player, UOItem item) {
        itemEquipHandler.equipItem(player, item);
    }

    @Override
    public void unequipItem(UOPlayer player, UnequipItem pickedUpItem) {
        itemEquipHandler.unequipItem(player, pickedUpItem);
    }
}
