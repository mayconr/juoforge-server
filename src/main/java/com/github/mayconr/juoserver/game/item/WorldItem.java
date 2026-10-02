package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface WorldItem {

    UOItem createItem(ItemRequest request, ItemTarget target);

    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options);

    void deleteItem(UOItem item);

    void deleteItem(int serial);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<UOContainer> getContainerBySerialId(int serial);

    /** Uses the storage visibility range; radius is currently retained for compatibility. */
    List<UOItem> getItemsInRange(Location location, int radius);

    List<ItemTemplate> getItemsTemplate(String stockType);

    ConsumeResult consumeItem(Integer container, String name, int amount, boolean searchNestedContainers);

    List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate);
}
