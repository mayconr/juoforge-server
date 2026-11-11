package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.ItemCreationResult.Created;
import com.github.mayconr.juoserver.game.item.ItemCreationResult.Rejected;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.network.packet.DropItem;
import lombok.RequiredArgsConstructor;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@lombok.extern.slf4j.Slf4j
@RequiredArgsConstructor
public class ItemModuleImpl implements ItemModule {

    private final WorldStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final ItemUseService itemUseService;
    private FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Deprecated
    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target) {
        return tryCreateItem(request, target).orThrow();
    }

    @Deprecated
    @Override
    public UOItem createItem(ItemRequest request, ItemTarget target, Consumer<ItemCreationOptions> options) {
        return tryCreateItem(request, target, options).orThrow();
    }

    @Override
    public ItemCreationResult tryCreateItem(ItemRequest request, ItemTarget target) {
        return tryCreateItem(request, target, null);
    }

    @Override
    public ItemCreationResult tryCreateItem(ItemRequest request, ItemTarget target,
                                            Consumer<ItemCreationOptions> options) {
        try {
            var context = new ItemCreationContext(request, target, options);
            var result = flows.execute(context);
            if (result.flowFailed()) {
                return rejectOrThrow(request, target, result);
            }
            return new Created(context.result("Cannot create item for request " + request));
        } catch (ModuleOperationException error) {
            log.error("Item creation failed: request={}, target={}, code={}, reason={}",
                    request, target, error.getCode(), error.getMessage(), error);
            throw error;
        } catch (RuntimeException error) {
            log.error("Unexpected item creation failure: request={}, target={}, reason={}",
                    request, target, error.getMessage(), error);
            throw ModuleOperationException.unexpected("createItem", error, "{item.creation_failed}");
        }
    }

    /** Returns game-rule failures as rejections and raises any other failure. */
    private ItemCreationResult rejectOrThrow(ItemRequest request, ItemTarget target, StepResult result) {
        var error = ModuleOperationException.fromFailure("createItem", result, "{item.creation_failed}");
        var reason = ItemCreationFailure.fromCode(error.getCode()).orElseThrow(() -> error);
        log.debug("Item creation rejected: request={}, target={}, reason={}, detail={}",
                request, target, reason, error.getMessage());
        return new Rejected(reason, error.getMessage());
    }

    @Override
    public void use(ItemUseContext context) {
        itemUseService.use(context);
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
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.getItemsInRange(location);
    }

    @Override
    public List<ItemTemplate> getItemsTemplate(String stockType) {
        return itemTemplateRegistry.getItemTemplates(stockType);
    }

    @Override
    public void deleteItem(UOItem item) {
        var result = flows.execute(new DeleteItemContext(item));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("deleteItem", result, null);
        }
    }

    @Override
    public void dropItem(UOPlayer player, DropItem dropItem) {
        flows.execute(DropItemContext.ofDropItem(player, dropItem));
    }

}
