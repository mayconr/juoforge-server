package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ItemModuleImpl implements ItemModule {

    private final ItemHandler itemHandler;
    private final ContainerHandler containerHandler;
    private final RealmStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final ItemUseService itemUseService;
    private FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target) {
        return createItem(request, target, null);
    }

    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
        try {
            var context = new ItemCreationContext(request, target, options);
            var result = flows.execute(context);
            if (result.flowFailed()) {
                throw ModuleOperationException.fromFailure("createItem", result, "{item.creation_failed}");
            }
            return context.result("Cannot create item for request " + request);
        } catch (ModuleOperationException error) {
            throw error;
        } catch (RuntimeException error) {
            throw ModuleOperationException.unexpected("createItem", error, "{item.creation_failed}");
        }
    }

    @Override
    public void use(ItemUseContext context) {
        itemUseService.use(context);
    }

    @Override
    public void openContainer(UOPlayer player, Container container) {
        flows.execute(new OpenContainerContext(player, container));
    }

    @Override
    public void deleteItem(int serial) {
        if (!UOItem.isItem(serial)) {
            throw new IllegalArgumentException("Serial [" + serial + "] is not an item");
        }
        final var item = getItemBySerialId(serial)
                .orElseThrow(() -> new IllegalArgumentException("Item [" + serial + "] not found"));
        deleteItem(item);
    }

    @Override
    public Optional<UOItem> getItemBySerialId(int serial) {
        return storage.getItem(serial);
    }

    @Override
    public Optional<UOContainer> getContainerBySerialId(int serial) {
        return storage.getContainer(serial);
    }

    @Override
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.getItemsInRange(location);
    }

    @Override
    public List<ItemTemplate> getItemsTemplate(String stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    @Override
    public void deleteItem(UOItem item) {
        itemHandler.deleteItem(item);
    }

    @Override
    public void dropItem(UOPlayer player, DropItem dropItem) {
        flows.execute(DropItemContext.ofDropItem(player, dropItem));
    }

    @Override
    public List<UOItem> getItemsInContainer(Integer containerSerial, Predicate<UOItem> predicate) {
        return containerHandler.getItemsInContainer(containerSerial, predicate);
    }

    @Override
    public ConsumeResult consumeItem(Integer containerSerial, String name, int amount, boolean searchNestedContainers) {
        int remaining = containerHandler.consumeItem(containerSerial, name, amount, searchNestedContainers);
        return new ConsumeResult(remaining > -1, remaining);
    }
}
