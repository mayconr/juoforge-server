package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.DropItem;

public interface ItemCommands {

    UOItem createItemAtLocation(ItemCreationRequest request, Location location);

    UOItem createEquippedItem(ItemCreationRequest request, UOMobile mobile);

    UOItem createItemInContainer(ItemCreationRequest request, Container container);

    UOItem createUnloadedItem(ItemCreationRequest request);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    void dropItemOnTheGround(UOPlayer player, DropItem droppedItem);

    void dropItemInContainer(UOPlayer player, DropItem dropItem);

    void dropItemInContainer(UOPlayer player, UOItem item, Container container);

    ConsumeResult consumeItem(Container container, String name, int amount, boolean searchNestedContainers);
}
