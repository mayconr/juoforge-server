package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.exception.ItemTemplateNotFoundException;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
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

    UOItem createItem(ItemRequest request, ItemTarget target) {
        final var item = internalCreateItem(request);

        // General properties
        item.setAmount(request.amount());
        item.setHue(request.hue());

        final GameEvent event = switch (target) {
            case EquipItemTarget equipItemTarget -> {
                final var mobile = equipItemTarget.mobile();
                mobile.equipItem(item);
                yield new EquippedItemCreated(mobile, item);
            }
            case GroundItemTarget worldLocation -> {
                item.setLocation(worldLocation.location());
                yield new GroundedItemCreated(item);
            }
            case ContainerItemTarget containerItemTarget -> handleContainerItem(item, containerItemTarget);
        };

        storage.cacheItem(item);
        eventBus.publish(event);

        return item;
    }

    private GameEvent handleContainerItem(UOItem item, ContainerItemTarget target) {
        final var container = target.container();

        UOItem updatedItem = item;

        // search for another similar and try to stack
        if (item.hasFlag(ItemFlag.STACKABLE) && target.options().tryStack()) {
            for (UOItem containerItem : container.getItemsInContainer()) {
                if (containerItem.getName().equals(item.getName())) {
                    containerItem.increaseAmount(item.getAmount());
                    updatedItem = containerItem;
                    break;
                }
            }
        }

        // Item is stackable, but is unable to stack. Should add to container
        if (updatedItem.equals(item)) {
            container.addItemToContainer(updatedItem);
        }

        return new ItemCreatedInContainer(container, updatedItem);
    }

    public UOItem createUnloadedItem(ItemRequest request) {
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

    private UOItem internalCreateItem(ItemRequest request) {
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
                new DefaultAttributeMap(template.attr()),
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

    private Supplier<ItemTemplate> supplierFactory(ItemRequest request) {
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
