package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.DropItem;

import java.util.function.Consumer;

public interface ItemCommands {

    UOItem createItem(ItemRequest request, ItemTarget target);

    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    UOItem createUnloadedItem(ItemRequest request);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    void dropItemOnTheGround(UOPlayer player, DropItem droppedItem);

    void dropItemInContainer(UOPlayer player, DropItem dropItem);

    void dropItemInContainer(UOPlayer player, UOItem item, Container container);

    ConsumeResult consumeItem(Container container, String name, int amount, boolean searchNestedContainers);
}
