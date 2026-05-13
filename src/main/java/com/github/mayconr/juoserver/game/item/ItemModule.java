package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.DropItem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ItemModule extends WorldModule {

    UOItem createItem(ItemRequest request, ItemTarget target);

    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    void deleteItem(UOItem item);

    void dropItem(UOPlayer player, DropItem dropItem);

    ConsumeResult consumeItem(Integer container, String name, int amount, boolean searchNestedContainers);

    List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate);
}
