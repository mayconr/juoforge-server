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
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class ItemHandler {

    private final SerialGenerator serialGenerator;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final RealmStorage storage;
    private final EventBus eventBus;

    UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> optionsConsumer) {
        final var item = internalCreateItem(request);

        final var options = new ItemCreationOptions();
        optionsConsumer.accept(options);

        // General properties
        item.setAmount(request.amount());
        item.setHue(request.hue());
        item.setDirection(request.direction());

        final GameEvent event = switch (target) {
            case EquipItemTarget equipItemTarget -> {
                final var mobile = equipItemTarget.mobile();
                mobile.equipItem(item);
                yield new EquippedItemCreated(mobile, item, options.renderOnCreate());
            }
            case GroundItemTarget worldLocation -> {
                item.setLocation(worldLocation.location());
                yield new GroundedItemCreated(item, options.renderOnCreate());
            }
            case ContainerItemTarget containerItemTarget -> handleContainerItem(item, containerItemTarget, options);
        };

        storage.cache(item);
        eventBus.publish(event);

        return item;
    }

    private GameEvent handleContainerItem(UOItem item, ContainerItemTarget target, ItemCreationOptions options) {
        final var container = target.container();

        UOItem updatedItem = item;

        // search for another similar and try to stack
        if (item.hasFlag(ItemFlag.STACKABLE) && target.options().tryStack()) {
            for (UOItem containerItem : container.getContainerItems()) {
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

        return new ItemCreatedInContainer(container, updatedItem, options.renderOnCreate());
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

        return storage.loadItem(serialGenerator.getNextItem(), template);
    }

    private Supplier<ItemTemplate> supplierFactory(ItemRequest request) {
        if (request.modelId() != null) {
            return ()->itemTemplateRegistry.get(request.modelId())
                    .getFirst();
        }

        if (request.name() != null) {
            return ()->itemTemplateRegistry.get(request.name());
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
        eventBus.publish(new ItemUpdated(item, null));
    }
}
