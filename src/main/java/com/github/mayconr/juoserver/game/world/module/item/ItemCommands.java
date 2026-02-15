package com.github.mayconr.juoserver.game.world.module.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.UnequipItem;

public interface ItemCommands {
    UOItem createItemAtLocation(String name, Location location);

    UOItem createEquippedItem(UOMobile mobile, String name);

    UOItem createContainerItem(String name, Container container);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    void dropItemOnTheGround(UOPlayer player, DropItem droppedItem);

    void dropItemInContainer(UOPlayer player, DropItem dropItem);

    void dropItemInContainer(UOPlayer player, UOItem item, Container container);

    void equipItem(UOPlayer player, UOItem item);

    void unequipItem(UOPlayer player, UnequipItem pickedUpItem);
}
