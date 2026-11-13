package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.GamePlaySettings.ItemDecay;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayClock;
import com.github.mayconr.juoserver.game.item.decay.ItemDecayState;
import com.github.mayconr.juoserver.game.item.flow.decay.DecayItemContext;
import com.github.mayconr.juoserver.game.item.flow.decay.DecayItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.property.Properties;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemDecayTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final Properties properties = mock(Properties.class);
    private final EventBus events = mock(EventBus.class);
    private final Map<Integer, UOItem> cache = new LinkedHashMap<>();
    private final AtomicReference<Long> savedClock = new AtomicReference<>(1_000_000L);
    private final FlowFacade facade = mock(FlowFacade.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private ItemModuleImpl module;
    private Flow<DecayItemContext> decay;
    private Flow<DropItemContext> drops;

    @BeforeEach
    void setUp() {
        when(storage.items()).thenReturn(items);
        when(storage.properties()).thenReturn(properties);
        when(properties.get(ItemDecayClock.KEY)).thenAnswer(call -> Optional.of(savedClock.get()));
        doAnswer(call -> {
            savedClock.set(call.getArgument(1));
            return null;
        }).when(properties).put(eq(ItemDecayClock.KEY), any(Long.class));
        when(items.get(anyInt())).thenAnswer(call -> Optional.ofNullable(cache.get(call.getArgument(0))));
        when(items.onGround()).thenAnswer(call ->
                cache.values().stream().filter(UOItem::isOnTheGround).toList());
        when(items.loadContainerItems(any())).thenReturn(CompletableFuture.completedFuture(List.of()));
        doAnswer(call -> {
            var item = call.<UOItem>getArgument(0);
            cache.remove(item.getSerialId());
            return null;
        }).when(items).delete(any());
        doAnswer(call -> {
            var item = call.<UOItem>getArgument(0);
            cache.put(item.getSerialId(), item);
            return null;
        }).when(items).placeOnGround(any());
        when(player.getSerialId()).thenReturn(1);
        when(player.getX()).thenReturn(100);
        when(player.getY()).thenReturn(100);
        initialize(new ItemDecay());
    }

    private void initialize(ItemDecay settings) {
        module = new ItemModuleImpl(storage, mock(ItemTemplateRegistry.class), mock(ItemUseService.class),
                new ItemQualityCalculator(), settings);
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(facade);
        module.initialize(context);
        var modules = GameModules.builder().item(module).build();
        var infra = GameInfra.builder().storage(storage).eventBus(events)
                .settings(new GamePlaySettings().withItems(new GamePlaySettings.Items(null, settings))).build();
        decay = DecayItemFlowDefinition.build(modules, infra);
        drops = DropItemFlowDefinition.build(modules, infra);
        var deletion = DeleteItemFlowDefinition.build(storage, events, mock(WorldUI.class));
        when(facade.execute(any(DecayItemContext.class)))
                .thenAnswer(call -> decay.execute(call.getArgument(0)));
        when(facade.execute(any(DeleteItemContext.class)))
                .thenAnswer(call -> deletion.execute(call.getArgument(0)));
        when(facade.execute(any(DropItemContext.class)))
                .thenAnswer(call -> drops.execute(call.getArgument(0)));
    }

    @Test
    void expiresAt120SecondsUsingPeriodicScansAndExistingDeletionEvent() {
        var item = ground(1);
        module.update(60);
        assertSame(item, cache.get(item.getSerialId()));
        module.update(60);
        assertFalse(cache.containsKey(item.getSerialId()));
        verify(events).publish(new ItemDeleted(item));
    }

    @Test
    void respectsConfiguredScanIntervalAndDuration() {
        initialize(new ItemDecay(10, 4));
        var item = ground(1);
        module.update(9);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.update(2);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.update(1);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void restartResumesSavedClockWithoutUsingWallTime() {
        var item = ground(1);
        module.update(60);
        long beforeRestart = module.decayNow();
        // Simulates persisted attributes reconstructed with the item after the process restarts.
        var restored = new UOItem(item.toData(), null);
        cache.put(restored.getSerialId(), restored);
        initialize(new ItemDecay());
        assertEquals(beforeRestart, module.decayNow());
        module.update(59);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.update(1);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void leavesImmovableItemsAndContainedItemsAlone() {
        var immovable = ground(1);
        immovable.setMovable(false);
        var contained = ground(2);
        contained.setCurrentLocation(ItemLocation.container(0x40000003));
        module.update(240);
        assertTrue(cache.containsKey(immovable.getSerialId()));
        assertTrue(cache.containsKey(contained.getSerialId()));
    }

    @Test
    void heldGroundItemCannotDecay() {
        var item = ground(1);
        module.heldItems().hold(player, item);
        module.update(120);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.heldItems().release(player);
        module.update(60);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void oldGroundItemsWithoutDropDateReceiveFullPeriodOnDiscovery() {
        var item = ground(1);
        module.setProperty(item, ItemDecayState.DROPPED_AT, null, false);
        module.update(60);
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, item, -1));
        module.update(60);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.update(60);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void stackInteractionRenewsDestinationDate() {
        var target = ground(1);
        module.update(60);
        var dropped = ground(2);
        drop(dropped, target.getSerialId(), 100);
        assertEquals(2, target.getAmount());
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, target, -1));
        module.update(60);
        assertTrue(cache.containsKey(target.getSerialId()));
        module.update(60);
        assertFalse(cache.containsKey(target.getSerialId()));
    }

    @Test
    void rejectedDropPreservesOriginalDate() {
        var item = ground(1);
        long original = ItemDecayState.droppedAt(module, item, -1);
        module.update(60);
        drop(item, -1, 104);
        assertEquals(original, ItemDecayState.droppedAt(module, item, -1));
        module.update(60);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void successfulDropStartsNewPeriod() {
        var item = ground(1);
        module.update(60);
        drop(item, -1, 101);
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, item, -1));
        module.update(60);
        assertTrue(cache.containsKey(item.getSerialId()));
        module.update(60);
        assertFalse(cache.containsKey(item.getSerialId()));
    }

    @Test
    void containerSpillsDirectChildrenAndNestedContainerRetainsItsContents() {
        var parent = container(1);
        var child = ground(2);
        var nested = container(3);
        var grandchild = ground(4);
        inside(parent, child);
        inside(parent, nested);
        inside(nested, grandchild);
        when(items.loadContainerItems(parent)).thenReturn(CompletableFuture.completedFuture(List.of(child, nested)));
        when(items.loadContainerItems(nested)).thenReturn(CompletableFuture.completedFuture(List.of(grandchild)));

        module.update(120);

        assertFalse(cache.containsKey(parent.getSerialId()));
        assertTrue(child.isOnTheGround());
        assertTrue(nested.isOnTheGround());
        assertEquals(parent.getX(), child.getX());
        assertEquals(parent.getZ(), child.getZ());
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, child, -1));
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, nested, -1));
        assertEquals(ItemLocation.container(nested.getSerialId()), grandchild.getCurrentLocation());
        module.update(60);
        assertTrue(cache.containsKey(nested.getSerialId()));
        module.update(60);
        assertFalse(cache.containsKey(nested.getSerialId()));
        assertTrue(grandchild.isOnTheGround());
        assertEquals(module.decayNow(), ItemDecayState.droppedAt(module, grandchild, -1));
    }

    @Test
    void loadsContentsEvenIfContainerHasNeverBeenOpened() {
        var parent = container(1);
        var child = ground(2);
        child.setCurrentLocation(ItemLocation.container(parent.getSerialId()));
        when(items.loadContainerItems(parent)).thenReturn(CompletableFuture.completedFuture(List.of(child)));
        assertTrue(parent.getContainerItems().isEmpty());
        module.update(120);
        assertFalse(cache.containsKey(parent.getSerialId()));
        assertTrue(child.isOnTheGround());
    }

    @Test
    void loadFailurePreservesContainerForLaterRetry() {
        var parent = container(1);
        when(items.loadContainerItems(parent))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Database unavailable")));
        module.update(120);
        assertTrue(cache.containsKey(parent.getSerialId()));
        verify(events, never()).publish(new ItemDeleted(parent));
    }

    @Test
    void revalidatesAfterLoadingBeforeDeletingContainer() {
        var parent = container(1);
        when(items.loadContainerItems(parent)).thenAnswer(call -> {
            module.heldItems().hold(player, parent);
            return CompletableFuture.completedFuture(List.of());
        });
        module.update(120);
        assertTrue(cache.containsKey(parent.getSerialId()));
    }

    @Test
    void containerWithHeldChildWaitsForHoldToEnd() {
        var parent = container(1);
        var child = ground(2);
        inside(parent, child);
        module.heldItems().hold(player, child);
        module.update(120);
        assertTrue(cache.containsKey(parent.getSerialId()));
        assertTrue(child.isInContainer());
    }

    private void drop(UOItem item, int target, int x) {
        assertTrue(module.heldItems().hold(player, item));
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08).writeInt(item.getSerialId()).writeShort(x).writeShort(100)
                .writeByte(0).writeByte(0).writeInt(target);
        try {
            module.dropItem(player, new DropItem(buffer));
        } finally {
            buffer.release();
        }
    }

    private UOItem ground(int offset) {
        var item = new UOItem(data(offset), null);
        cache.put(item.getSerialId(), item);
        module.recordGroundInteraction(item);
        return item;
    }

    private UOContainer container(int offset) {
        var item = new UOContainer(data(offset), null);
        cache.put(item.getSerialId(), item);
        module.recordGroundInteraction(item);
        return item;
    }

    private static void inside(UOContainer parent, UOItem item) {
        parent.addItemToContainer(item);
        item.setCurrentLocation(ItemLocation.container(parent.getSerialId()));
    }

    private static UOItemData data(int offset) {
        var data = new UOItemData();
        data.setSerialId(0x40000000 + offset);
        data.setName("item");
        data.setAmount(1);
        data.setFlags(List.of(ItemFlag.STACKABLE));
        data.setPersistentAttrMap(new DefaultAttributeMap());
        data.setLocationType(ItemLocationType.GROUND);
        data.setX(100);
        data.setY(100);
        data.setZ(3);
        return data;
    }
}
