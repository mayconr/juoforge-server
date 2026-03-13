package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.exxception.ItemTemplateNotFoundException;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class ItemHandler {

    private final SerialGenerator serialGenerator;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final RealmStorage storage;
    private final EventBus eventBus;

    public UOItem createItemAtLocation(ItemCreationRequest request, Location location) {
        final var item = internalCreateItem(request);

        item.setLocation(location);
        item.setAmount(request.amount());
        item.setHue(request.hue());

        storage.cacheItem(item);
        eventBus.publish(new GroundedItemCreated(item));
        return item;
    }

    public UOItem createEquippedItem(ItemCreationRequest request, UOMobile mobile) {
        final var item = internalCreateItem(request);

        mobile.equipItem(item);

        storage.cacheItem(item);
        eventBus.publish(new EquippedItemCreated(mobile, item));
        return item;
    }

    public UOItem createItemInContainer(ItemCreationRequest request, Container container) {
        final var item = internalCreateItem(request);

        item.setAmount(request.amount());
        item.setHue(request.hue());
        container.addItemToContainer(item);

        storage.cacheItem(item);
        eventBus.publish(new ItemCreatedInContainer(container, item));
        return item;
    }

    public UOItem createUnloadedItem(ItemCreationRequest request) {
        final var amount = request.amount() == 0 ? 1 : request.amount();
        final var hue = request.hue();
        final var location = new PointInTheWorld(0,0,0);

        try {
            final var item = internalCreateItem(request);

            item.setHue(hue);
            item.setAmount(amount);
            item.setLocation(location);

            return item;
        } catch (NoSuchElementException e) {
            throw new ItemTemplateNotFoundException("ModelId " + request.modelId() + " does not exist");
        }
    }

    private UOItem internalCreateItem(ItemCreationRequest request) {
        final var template = supplierFactory(request).get();
        if (template == null) {
            throw new ItemTemplateNotFoundException("Template does not exist");
        }

        final var item = new UOItem(
                UUID.randomUUID(),
                serialGenerator.getNextItem(),
                template.modelId(),
                0,
                0,
                0,
                template.name(),
                template.displayName(),
                template.attr(),
                template.layer(),
                1,
                template.hue(),
                template.movable(),
                false,
                Direction.NORTH,
                null,
                template.flags()
        );
        if (template.flags().contains(ItemFlag.CONTAINER)) {
            return new UOContainer(item, Optional.ofNullable(template.attr().get("gumpId"))
                    .map(Integer.class::cast).orElse(0));
        }
        return item;
    }

    private Supplier<ItemTemplate> supplierFactory(ItemCreationRequest request) {
        if (request.modelId() != null) {
            return ()->itemTemplateRegistry.get(request.modelId())
                    .getFirst();
        }

        if (request.itemName() != null) {
            return ()->itemTemplateRegistry.get(request.itemName());
        }

        if (request.template() != null) {
            return request::template;
        }

        throw new ItemTemplateNotFoundException("Template not found");
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
