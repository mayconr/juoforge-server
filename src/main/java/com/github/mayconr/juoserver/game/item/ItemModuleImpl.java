package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.ItemCreationResult.Created;
import com.github.mayconr.juoserver.game.item.ItemCreationResult.Rejected;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.flow.flip.FlipItemContext;
import com.github.mayconr.juoserver.game.item.flow.update.UpdateItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetContainer;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetGround;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.game.model.GroundItemTarget;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.item.flow.equip.EquipHeldItemContext;
import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.equip.EquipItemContext;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.UnequipItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.GamePlaySettings.ItemDecay;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayClock;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayService;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.game.model.AttributeMap;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ItemModuleImpl implements ItemModule {

    private final WorldStorage storage;
    private final ItemTemplateRegistry itemTemplateRegistry;
    private final ItemUseService itemUseService;
    private final ItemQualityCalculator qualityCalculator;
    private final HeldItems heldItems = new HeldItems();
    private FlowFacade flows;
    private ItemDecay decaySettings = new ItemDecay();
    private ItemDecayClock decayClock;
    private ItemDecayService decayService;

    public ItemModuleImpl(
            WorldStorage storage,
            ItemTemplateRegistry templates,
            ItemUseService useService,
            ItemQualityCalculator quality,
            ItemDecay decaySettings
    ) {
        this(storage, templates, useService, quality);
        this.decaySettings = decaySettings;
    }

    public ItemModuleImpl(WorldStorage storage, ItemTemplateRegistry itemTemplateRegistry, ItemUseService itemUseService) {
        this(storage, itemTemplateRegistry, itemUseService, new ItemQualityCalculator());
    }

    @Override
    public void setProperty(UOItem item, String key, Object value, boolean runtimeOnly) {
        synchronized (Objects.requireNonNull(item, "Item is required")) {
            properties(item, runtimeOnly).set(Objects.requireNonNull(key, "Property key is required"), value);
        }
    }

    @Override
    public Object getProperty(UOItem item, String key, boolean runtimeOnly) {
        return getProperty(item, key, null, runtimeOnly);
    }

    @Override
    public <T> T getProperty(UOItem item, String key, T defaultValue, boolean runtimeOnly) {
        synchronized (Objects.requireNonNull(item, "Item is required")) {
            return properties(item, runtimeOnly).getOrDefault(
                    Objects.requireNonNull(key, "Property key is required"), defaultValue
            );
        }
    }

    @Override
    public <T> T computeProperty(UOItem item, String key, BiFunction<String, T, T> update, boolean runtimeOnly) {
        synchronized (Objects.requireNonNull(item, "Item is required")) {
            return properties(item, runtimeOnly).compute(
                    Objects.requireNonNull(key, "Property key is required"),
                    Objects.requireNonNull(update, "Property update is required")
            );
        }
    }

    private AttributeMap properties(UOItem item, boolean runtimeOnly) {
        return runtimeOnly ? item.runtimeAttributes() : item.persistentAttributes();
    }

    @Override
    public float calculateQuality(UOMobile mobile, double modifier) {
        return qualityCalculator.calculate(mobile, modifier);
    }

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
        this.decayClock = new ItemDecayClock(storage.properties());
        this.decayService = new ItemDecayService(storage.items(), decayClock, decaySettings, flows);
    }

    @Override
    public void update(double delta) {
        decayService.update(delta);
    }

    @Override
    public long decayNow() {
        return decayClock.now();
    }

    @Override
    public void recordGroundInteraction(UOItem item) {
        if (item.isOnTheGround()) {
            ItemDecayState.recordDrop(this, item, decayNow());
        }
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
            var item = context.result("Cannot create item for request " + request);
            recordGroundInteraction(item);
            return new Created(item);
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
    public void updateItem(UOItem item, ItemChange change) {
        Objects.requireNonNull(item, "Item is required");
        Objects.requireNonNull(change, "Item change is required");
        if (change.isEmpty()) {
            return;
        }
        var result = change.flip()
                ? flows.execute(new FlipItemContext(item))
                : flows.execute(new UpdateItemContext(item, change));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("updateItem", result, null);
        }
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
        return storage.items().get(serial);
    }

    @Override
    public List<UOItem> getItemsInRange(Location location, int radius) {
        return storage.items().inRange(location);
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
    public void liftItem(UOPlayer player, UnequipItem request) {
        flows.execute(new LiftItemContext(player, request));
    }

    @Override
    public void dropItem(UOPlayer player, DropItem dropItem) {
        synchronized (heldItems) {
            try {
                flows.execute(DropItemContext.ofDropItem(player, dropItem));
            } finally {
                heldItems.release(player);
            }
        }
    }

    @Override
    public StepResult dropItem(UOMobile mobile, UOItem item, ItemTarget target) {
        Objects.requireNonNull(mobile, "Mobile is required");
        Objects.requireNonNull(item, "Item is required");
        var context = switch (target) {
            case GroundItemTarget ground when ground.location() != null ->
                    DropItemContext.ofGround(mobile, item, new TargetGround(ground.location()));
            case ContainerItemTarget inside when inside.container() instanceof UOContainer container ->
                    DropItemContext.ofContainer(mobile, item, new TargetContainer(container, new PointInTheWorld(0, 0, 0)));
            case null, default -> null;
        };
        if (context == null) {
            return StepResult.failure("DROP_INVALID_TARGET", "Drop requires a ground location or a container");
        }
        synchronized (heldItems) {
            if (heldItems.isHeld(item)) {
                return StepResult.failure("DROP_ITEM_HELD", "Item is held by a player");
            }
            return flows.execute(context);
        }
    }

    @Override
    public void equipHeldItem(UOPlayer player, EquipItemRequest request) {
        try {
            flows.execute(new EquipHeldItemContext(player, request));
        } finally {
            heldItems.release(player);
        }
    }

    @Override
    public boolean equipItem(UOMobile mobile, UOItem item) {
        return equipItem(mobile, item, true);
    }

    @Override
    public boolean equipItem(UOMobile mobile, UOItem item, boolean notify) {
        Objects.requireNonNull(mobile, "Mobile is required");
        Objects.requireNonNull(item, "Item is required");
        var context = new EquipItemContext(mobile, item);
        context.setNotify(notify);
        var result = flows.execute(context);
        return !result.flowFailed() && context.isEquipped();
    }

    @Override
    public boolean unequipItem(UOMobile mobile, UOItem item) {
        return unequipItem(mobile, item, ItemTarget.orphan());
    }

    @Override
    public boolean unequipItem(UOMobile mobile, UOItem item, ItemTarget target) {
        Objects.requireNonNull(mobile, "Mobile is required");
        Objects.requireNonNull(item, "Item is required");
        var context = new UnequipItemContext(mobile, item, Objects.requireNonNull(target, "Item target is required"));
        var result = flows.execute(context);
        return !result.flowFailed() && context.isUnequipped();
    }

    @Override
    public void releaseHeldItem(UOPlayer player) {
        heldItems.release(player);
    }

    @Override
    public HeldItems heldItems() {
        return heldItems;
    }

}
