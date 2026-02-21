package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemHandler {

    private final SerialGenerator serialGenerator;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final RealmStorage storage;
    private final EventBus eventBus;

    public UOItem createItemAtLocation(String name, int amount, Location location) {
        final var item = internalCreateItemBeTemplate(name, location, 1);

        storage.cacheItem(item);
        eventBus.publish(new GroundedItemCreated(item));
        return item;
    }

    public UOItem createEquippedItem(UOMobile mobile, String name) {
        final var item = internalCreateItemBeTemplate(name, new PointInTheWorld(0,0,0), 1);
        mobile.equipItem(item);

        storage.cacheItem(item);
        eventBus.publish(new EquippedItemCreated(mobile, item));
        return item;
    }

    public UOItem createItemInContainer(String name, int amount, Container container) {
        final var item = internalCreateItemBeTemplate(name, new PointInTheWorld(0,0,0), amount);
        container.addItemToContainer(item);
        storage.cacheItem(item);
        eventBus.publish(new ItemCreatedInContainer(container, item));
        return item;
    }

    private UOItem internalCreateItemBeTemplate(String name, Location location, int amount) {
        final var template = itemTemplateRegistry.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Item template ["+name+"] not found");
        }
        return ItemFactory.createFromTemplate(serialGenerator, template, location, amount);
    }

    public void deleteItem(UOItem item) {
        storage.deleteItem(item);
        eventBus.publish(new ItemDeleted(item));
    }

    public void moveItem(UOItem item, Location location) {
        item.setLocation(location);
        eventBus.publish(new ItemUpdated(item));
    }
}
