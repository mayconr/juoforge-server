package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.container.ContainerModule;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.flow.drop.notification.RejectDropHook;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.item.hold.RestoreHeldItem;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.event.ItemDropAccepted;
import com.github.mayconr.juoserver.game.model.event.ItemDropRejected;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.EnumMap;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DropItemValidationTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final Mobiles mobiles = mock(Mobiles.class);
    private final EventBus events = mock(EventBus.class);
    private final ContainerModule containers = mock(ContainerModule.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private final HeldItems held = new HeldItems();
    private final UOItem dropped = new UOItem(data(0x40000001), null);
    private Flow<DropItemContext> flow;

    @BeforeEach
    void setUp() {
        when(storage.items()).thenReturn(items);
        when(storage.mobiles()).thenReturn(mobiles);
        when(player.getSerialId()).thenReturn(1);
        when(player.getX()).thenReturn(100);
        when(player.getY()).thenReturn(100);
        when(mobiles.get(1)).thenReturn(Optional.of(player));
        var module = mock(ItemModule.class);
        when(module.heldItems()).thenReturn(held);
        flow = DropItemFlowDefinition.build(
                GameModules.builder().item(module).container(containers).build(),
                GameInfra.builder().storage(storage).eventBus(events).build()
        );
        stored(dropped);
        held.hold(player, dropped);
    }

    @Test
    void rejectsContainerDroppedInsideItselfWithoutDetachingIt() {
        var container = container(dropped.getSerialId());
        var original = container.getCurrentLocation();
        assertEquals("DROP_INVALID_CONTAINMENT", drop(container.getSerialId(), 100).code());
        assertEquals(original, container.getCurrentLocation());
        verifyRejected();
    }

    @Test
    void rejectsContainerDroppedInsideItsDescendant() {
        var parent = container(dropped.getSerialId());
        var child = container(0x40000002);
        child.setCurrentLocation(ItemLocation.container(parent.getSerialId()));
        parent.addItemToContainer(child);
        assertEquals("DROP_INVALID_CONTAINMENT", drop(child.getSerialId(), 100).code());
        assertTrue(parent.isOnTheGround());
        assertTrue(parent.getContainerItems().contains(child.getSerialId()));
        verifyRejected();
    }

    @ParameterizedTest
    @ValueSource(strings = {"self", "flag", "graphic", "hue", "name", "overflow", "equipped", "orphan", "held"})
    void rejectsInvalidStacksWithoutChangingEitherItem(String invalid) {
        var target = stored(new UOItem(data(0x40000002), null));
        switch (invalid) {
            case "self" -> target = dropped;
            case "flag" -> target.setFlags(List.of());
            case "graphic" -> target.setModelId(2);
            case "hue" -> target.setHue(2);
            case "name" -> target.setName("different");
            case "overflow" -> target.setAmount(Integer.MAX_VALUE);
            case "equipped" -> target.setCurrentLocation(ItemLocation.equipped(1));
            case "orphan" -> target.setCurrentLocation(ItemLocation.orphan());
            case "held" -> {
                var other = mock(UOPlayer.class);
                when(other.getSerialId()).thenReturn(2);
                held.hold(other, target);
            }
            default -> throw new IllegalArgumentException(invalid);
        }
        int amount = target.getAmount();
        var original = dropped.getCurrentLocation();
        assertTrue(drop(target.getSerialId(), 100).flowFailed());
        assertEquals(original, dropped.getCurrentLocation());
        assertEquals(1, dropped.getAmount());
        assertEquals(amount, target.getAmount());
        verifyRejected();
    }

    @Test
    void rejectsDistantGroundDropRegardlessOfItemOrigin() {
        assertEquals("DROP_OUT_OF_RANGE", drop(-1, 103).code());
        assertTrue(dropped.isOnTheGround());
        verifyRejected();
    }

    @Test
    void rejectsDistantDestinationEvenWhenDroppedItemIsNear() {
        var target = container(0x40000002);
        target.setX(103);
        assertEquals("DROP_OUT_OF_RANGE", drop(target.getSerialId(), 100).code());
        assertTrue(dropped.isOnTheGround());
        verifyRejected();
    }

    @Test
    void rejectsExistingContainerCycleWithoutChangingItems() {
        var first = container(0x40000002);
        var second = container(0x40000003);
        first.setCurrentLocation(ItemLocation.container(second.getSerialId()));
        second.setCurrentLocation(ItemLocation.container(first.getSerialId()));
        assertEquals("DROP_INVALID_CONTAINMENT", drop(first.getSerialId(), 100).code());
        assertTrue(dropped.isOnTheGround());
        verifyRejected();
    }

    @Test
    void rejectsStackWhoseParentContainerIsMissing() {
        var target = stored(new UOItem(data(0x40000002), null));
        target.setCurrentLocation(ItemLocation.container(0x40000003));
        assertEquals("DROP_INVALID_CONTAINMENT", drop(target.getSerialId(), 100).code());
        assertTrue(dropped.isOnTheGround());
        assertEquals(1, target.getAmount());
        verifyRejected();
    }

    @Test
    void rejectsOtherMobilesBackpack() {
        var target = container(0x40000002);
        target.setCurrentLocation(ItemLocation.equipped(2));
        var other = mock(UOPlayer.class);
        when(other.getSerialId()).thenReturn(2);
        when(other.getBackpack()).thenReturn(target.getSerialId());
        when(mobiles.get(2)).thenReturn(Optional.of(other));
        assertEquals("DROP_ACCESS_DENIED", drop(2, 100).code());
        assertTrue(dropped.isOnTheGround());
        verifyRejected();
    }

    @Test
    void acceptsOwnNestedContainerUsingOwnersPosition() {
        var backpack = container(0x40000002);
        backpack.setCurrentLocation(ItemLocation.equipped(1));
        var pouch = container(0x40000003);
        pouch.setCurrentLocation(ItemLocation.container(backpack.getSerialId()));
        pouch.setLocation(500, 500);
        assertTrue(drop(pouch.getSerialId(), 0).shouldContinue());
        verify(containers).addItem(pouch, dropped, false);
        verify(events).publish(new ItemDropAccepted(player));
    }

    @Test
    void acceptsStackAtLimitAndDeletesOnlyDroppedItem() {
        var target = stored(new UOItem(data(0x40000002), null));
        target.setAmount(59999);
        assertTrue(drop(target.getSerialId(), 100).shouldContinue());
        assertEquals(60000, target.getAmount());
        verify(items).delete(dropped);
        verify(events).publish(new ItemDropAccepted(player));
        assertFalse(held.isHoldingAny(player));
    }

    @Test
    void refusedGroundDropRestoresLiftPositionEvenIfPlayerMoved() {
        dropped.setLocation(90, 90);
        assertEquals("DROP_OUT_OF_RANGE", drop(-1, 103).code());
        assertEquals(100, dropped.getX());
        assertEquals(100, dropped.getY());
        verify(events).publish(new ItemDropRejected(player, LiftRejectReason.OUT_OF_RANGE, dropped));
        assertFalse(held.isHoldingAny(player));
    }

    @Test
    void refusedDropRestoresItemUnequippedDuringLift() {
        held.release(player);
        dropped.setLayer(Layer.BACKPACK);
        dropped.setCurrentLocation(ItemLocation.equipped(1));
        held.hold(player, dropped);
        dropped.setCurrentLocation(ItemLocation.orphan());
        var equipment = new EnumMap<Layer, Integer>(Layer.class);
        when(player.getEquippedItems()).thenReturn(equipment);
        when(player.isLayerAvailable(Layer.BACKPACK)).thenReturn(true);
        doAnswer(invocation -> {
            equipment.put(dropped.getLayer(), dropped.getSerialId());
            return null;
        }).when(player).equipItem(dropped);

        assertTrue(drop(-1, 103).flowFailed());
        assertEquals(ItemLocation.equipped(1), dropped.getCurrentLocation());
        assertEquals(dropped.getSerialId(), equipment.get(Layer.BACKPACK));
        verifyRejected();
    }

    @Test
    void placementExceptionRestoresContainerMembershipAndCoordinates() {
        var origin = container(0x40000003);
        held.release(player);
        dropped.setCurrentLocation(ItemLocation.container(origin.getSerialId()));
        dropped.setLocation(20, 30);
        origin.addItemToContainer(dropped);
        held.hold(player, dropped);
        var target = container(0x40000002);
        doAnswer(invocation -> {
            target.addItemToContainer(dropped);
            dropped.setCurrentLocation(ItemLocation.container(target.getSerialId()));
            throw new IllegalStateException("Placement failed");
        }).when(containers).addItem(target, dropped, false);

        assertEquals("FLOW_EXCEPTION", drop(target.getSerialId(), 5).code());
        assertEquals(ItemLocation.container(origin.getSerialId()), dropped.getCurrentLocation());
        assertTrue(origin.getContainerItems().contains(dropped.getSerialId()));
        assertFalse(target.getContainerItems().contains(dropped.getSerialId()));
        assertEquals(20, dropped.getX());
        assertEquals(30, dropped.getY());
        verify(events).publish(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, dropped));
        verifyNoMoreInteractions(events);
        assertFalse(held.isHoldingAny(player));
    }

    @Test
    void failureDeletingStackRestoresTargetAndDroppedItem() {
        var target = stored(new UOItem(data(0x40000002), null));
        target.setAmount(10);
        target.setQuality(0.5f);
        doThrow(new IllegalStateException("Deletion failed")).when(items).delete(dropped);

        assertEquals("FLOW_EXCEPTION", drop(target.getSerialId(), 100).code());
        assertEquals(10, target.getAmount());
        assertEquals(0.5f, target.getQuality());
        assertTrue(dropped.isOnTheGround());
        verifyRejected();
    }

    @Test
    void invalidSerialRestoresActualHeldItemWithoutMovingRequestedItem() {
        var unrelated = stored(new UOItem(data(0x40000002), null));
        assertEquals("ITEM_NOT_HELD", drop(unrelated.getSerialId(), -1, 100).code());
        assertTrue(unrelated.isOnTheGround());
        verifyRejected();
    }

    @Test
    void dropWithoutHeldItemOnlySendsRejection() {
        held.release(player);
        assertEquals("ITEM_NOT_HELD", drop(-1, 100).code());
        verify(events).publish(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, null));
        verifyNoMoreInteractions(events);
    }

    @Test
    void missingLiftOriginFallsBackToPlayersFeet() {
        held.release(player);
        dropped.setCurrentLocation(ItemLocation.container(0x40000003));
        held.hold(player, dropped);
        assertTrue(drop(-1, 103).flowFailed());
        assertTrue(dropped.isOnTheGround());
        assertEquals(player.getX(), dropped.getX());
        assertEquals(player.getY(), dropped.getY());
        verifyRejected();
    }

    @Test
    void occupiedOriginalEquipmentLayerIsPreserved() {
        held.release(player);
        dropped.setLayer(Layer.ONE_HANDED);
        dropped.setCurrentLocation(ItemLocation.equipped(1));
        held.hold(player, dropped);
        dropped.setCurrentLocation(ItemLocation.orphan());
        var equipment = new EnumMap<Layer, Integer>(Layer.class);
        equipment.put(Layer.ONE_HANDED, 0x40000003);
        when(player.getEquippedItems()).thenReturn(equipment);

        assertTrue(drop(-1, 103).flowFailed());
        assertTrue(dropped.isOnTheGround());
        assertEquals(0x40000003, equipment.get(Layer.ONE_HANDED));
        verifyRejected();
    }

    @Test
    void successfulGroundDropPublishesAcceptanceAfterPlacement() {
        assertTrue(drop(-1, 101).shouldContinue());
        assertEquals(101, dropped.getX());
        verify(events).publish(new ItemDropAccepted(player));
        assertFalse(held.isHoldingAny(player));
    }

    @Test
    void restorationFailureStillReleasesHoldAndNotifiesOnlyOnce() {
        var restorer = mock(RestoreHeldItem.class);
        doThrow(new IllegalStateException("Storage unavailable")).when(restorer).restore(any(), any());
        var hook = new RejectDropHook(held, restorer, events);
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08).writeInt(dropped.getSerialId()).writeShort(100).writeShort(100)
                .writeByte(0).writeByte(0).writeInt(-1);
        try {
            var context = DropItemContext.ofDropItem(player, new DropItem(buffer));
            var failure = StepResult.failure("DROP_OUT_OF_RANGE", "Out of reach");
            assertThrows(IllegalStateException.class, () -> hook.execute("placement", failure, context));
            hook.execute("placement", failure, context);
            verifyRejected();
        } finally {
            buffer.release();
        }
    }

    private void verifyRejected() {
        verify(events).publish(isA(ItemDropRejected.class));
        verifyNoMoreInteractions(events);
        verifyNoInteractions(containers);
        assertFalse(held.isHoldingAny(player));
    }

    private StepResult drop(int target, int x) {
        return drop(dropped.getSerialId(), target, x);
    }

    private StepResult drop(int serial, int target, int x) {
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08).writeInt(serial).writeShort(x).writeShort(100)
                .writeByte(0).writeByte(0).writeInt(target);
        try {
            return flow.execute(DropItemContext.ofDropItem(player, new DropItem(buffer)));
        } finally {
            buffer.release();
        }
    }

    private UOContainer container(int serial) {
        var container = new UOContainer(data(serial), null);
        stored(container);
        when(items.getContainer(serial)).thenReturn(Optional.of(container));
        return container;
    }

    private UOItem stored(UOItem item) {
        when(items.get(item.getSerialId())).thenReturn(Optional.of(item));
        return item;
    }

    private static UOItemData data(int serial) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setName("stack");
        data.setModelId(1);
        data.setAmount(1);
        data.setFlags(List.of(ItemFlag.STACKABLE));
        data.setLocationType(ItemLocationType.GROUND);
        data.setX(100);
        data.setY(100);
        return data;
    }
}
