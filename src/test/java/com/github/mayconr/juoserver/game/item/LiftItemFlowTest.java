package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemFlowDefinition;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemLiftRejected;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LiftItemFlowTest {
    private static final int PLAYER = 0x00000001;
    private static final int OTHER_PLAYER = 0x00000002;
    private static final int ITEM = 0x40000001;
    private static final int BACKPACK = 0x40000002;
    private static final int POUCH = 0x40000003;

    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final Mobiles mobiles = mock(Mobiles.class);
    private final EventBus eventBus = mock(EventBus.class);
    private final MobileModule mobileModule = mock(MobileModule.class);
    private final HeldItems heldItems = new HeldItems();
    private final UOPlayer player = player(PLAYER);

    private Flow<LiftItemContext> flow;

    @BeforeEach
    void setUp() {
        when(storage.items()).thenReturn(items);
        when(storage.mobiles()).thenReturn(mobiles);
        when(items.get(anyInt())).thenReturn(Optional.empty());
        when(mobiles.get(anyInt())).thenReturn(Optional.empty());
        when(mobiles.get(PLAYER)).thenReturn(Optional.of(player));

        var itemModule = mock(ItemModule.class);
        when(itemModule.heldItems()).thenReturn(heldItems);
        var modules = GameModules.builder().item(itemModule).mobile(mobileModule).build();
        var infra = GameInfra.builder().storage(storage).eventBus(eventBus).build();
        flow = LiftItemFlowDefinition.build(modules, infra);
    }

    @Test
    void groundItemInRangeIsHeld() {
        stored(groundItem(ITEM, 102, 98));

        assertLifted(lift(ITEM, 1));
    }

    @Test
    void groundItemOutOfRangeIsRefused() {
        stored(groundItem(ITEM, 103, 100));

        assertRefused(lift(ITEM, 1), LiftRejectReason.OUT_OF_RANGE);
    }

    @Test
    void itemNestedInsideThePlayersBackpackIsHeldAtAnyDistance() {
        stored(equippedItem(BACKPACK, PLAYER));
        stored(containedItem(POUCH, BACKPACK));
        stored(containedItem(ITEM, POUCH));

        assertLifted(lift(ITEM, 1));
    }

    @Test
    void itemInsideAnotherMobilesBackpackIsRefusedAsTheft() {
        UOMobile owner = player(OTHER_PLAYER);
        when(mobiles.get(OTHER_PLAYER)).thenReturn(Optional.of(owner));
        stored(equippedItem(BACKPACK, OTHER_PLAYER));
        stored(containedItem(ITEM, BACKPACK));

        assertRefused(lift(ITEM, 1), LiftRejectReason.TRY_TO_STEAL);
    }

    @Test
    void itemOutsideTheWorldIsRefused() {
        stored(containedItem(ITEM, BACKPACK));

        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @Test
    void containerInsideItselfIsRefused() {
        stored(containedItem(ITEM, ITEM));

        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @Test
    void unknownItemIsRefused() {
        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @Test
    void immovableItemIsRefused() {
        var item = groundItem(ITEM, 100, 100);
        item.setMovable(false);
        stored(item);

        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @ParameterizedTest
    @CsvSource({"50, 49, false", "50, 0, false", "50, 50, true", "50, 60, true", "1, 0, true"})
    void stacksAreLiftedOnlyWhole(int amount, int requested, boolean lifted) {
        var item = groundItem(ITEM, 100, 100);
        item.setAmount(amount);
        stored(item);

        var context = lift(ITEM, requested);

        if (lifted) {
            assertLifted(context);
        } else {
            assertRefused(context, LiftRejectReason.CANNOT_LIFT);
        }
    }

    @Test
    void playerAlreadyHoldingAnItemIsRefused() {
        var held = groundItem(BACKPACK, 100, 100);
        heldItems.hold(player, held);
        stored(groundItem(ITEM, 100, 100));

        assertRefused(lift(ITEM, 1), LiftRejectReason.ARE_HOLDING);
        assertTrue(heldItems.isHolding(player, BACKPACK));
    }

    @Test
    void itemHeldByAnotherPlayerIsRefused() {
        var item = groundItem(ITEM, 100, 100);
        stored(item);
        heldItems.hold(player(OTHER_PLAYER), item);

        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @Test
    void deadPlayerIsRefused() {
        when(player.isAlive()).thenReturn(false);
        stored(groundItem(ITEM, 100, 100));

        assertRefused(lift(ITEM, 1), LiftRejectReason.INSPECIFIC);
    }

    @Test
    void equippedItemIsTakenOffWhenLifted() {
        var item = stored(equippedItem(ITEM, PLAYER));
        when(mobileModule.unequipItem(player, item)).thenReturn(true);

        assertLifted(lift(ITEM, 1));
        verify(mobileModule).unequipItem(player, item);
    }

    @Test
    void equippedItemThatCannotBeTakenOffFreesTheCursor() {
        var item = stored(equippedItem(ITEM, PLAYER));
        when(mobileModule.unequipItem(player, item)).thenReturn(false);

        assertRefused(lift(ITEM, 1), LiftRejectReason.CANNOT_LIFT);
    }

    @Test
    void itemOnTheGroundIsNotUnequipped() {
        stored(groundItem(ITEM, 100, 100));

        lift(ITEM, 1);

        verify(mobileModule, never()).unequipItem(any(), any());
    }

    private LiftItemContext lift(int serial, int amount) {
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x07);
        buffer.writeInt(serial);
        buffer.writeShort(amount);
        try {
            var context = new LiftItemContext(player, new UnequipItem(buffer));
            flow.execute(context);
            return context;
        } finally {
            buffer.release();
        }
    }

    private void assertLifted(LiftItemContext context) {
        assertTrue(context.isHeld());
        assertTrue(heldItems.isHolding(player, ITEM));
        verify(eventBus, never()).publish(any());
    }

    private void assertRefused(LiftItemContext context, LiftRejectReason reason) {
        assertFalse(context.isHeld());
        assertFalse(heldItems.isHolding(player, ITEM));
        verify(eventBus).publish(new ItemLiftRejected(player, reason));
        assertEquals(reason, context.getRejection());
    }

    private UOItem stored(UOItem item) {
        when(items.get(item.getSerialId())).thenReturn(Optional.of(item));
        return item;
    }

    private static UOPlayer player(int serial) {
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(serial);
        when(player.getX()).thenReturn(100);
        when(player.getY()).thenReturn(100);
        when(player.isAlive()).thenReturn(true);
        return player;
    }

    private static UOItem groundItem(int serial, int x, int y) {
        var data = data(serial, ItemLocationType.GROUND);
        data.setX(x);
        data.setY(y);
        return new UOItem(data, null);
    }

    private static UOItem equippedItem(int serial, int owner) {
        var data = data(serial, ItemLocationType.EQUIPPED);
        data.setOwnerSerialId(owner);
        return new UOItem(data, null);
    }

    private static UOItem containedItem(int serial, int container) {
        var data = data(serial, ItemLocationType.CONTAINER);
        data.setContainerSerialId(container);
        data.setX(500);
        data.setY(500);
        return new UOItem(data, null);
    }

    private static UOItemData data(int serial, ItemLocationType location) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setAmount(1);
        data.setLocationType(location);
        return data;
    }
}
