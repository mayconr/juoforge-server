package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.item.hold.RequireHeldItemStep;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.DropItem;
import io.netty.buffer.Unpooled;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeldItemsTest {
    private final HeldItems heldItems = new HeldItems();
    private final UOPlayer player = player(1);
    private final UOPlayer otherPlayer = player(2);
    private final UOItem item = item(0x40000001);
    private final UOItem otherItem = item(0x40000002);

    @Test
    void playerHoldsTheLiftedItem() {
        assertTrue(heldItems.hold(player, item));

        assertTrue(heldItems.isHoldingAny(player));
        assertTrue(heldItems.isHolding(player, item.getSerialId()));
        assertFalse(heldItems.isHolding(player, otherItem.getSerialId()));
    }

    @Test
    void itemCannotBeHeldByTwoPlayers() {
        heldItems.hold(player, item);

        assertFalse(heldItems.hold(otherPlayer, item));
        assertFalse(heldItems.isHoldingAny(otherPlayer));
    }

    @Test
    void playerCannotHoldTwoItems() {
        heldItems.hold(player, item);

        assertFalse(heldItems.hold(player, otherItem));
        assertTrue(heldItems.isHolding(player, item.getSerialId()));
        assertTrue(heldItems.hold(otherPlayer, otherItem));
    }

    @Test
    void releaseFreesBothThePlayerAndTheItem() {
        heldItems.hold(player, item);

        assertEquals(OptionalInt.of(item.getSerialId()), heldItems.release(player));

        assertFalse(heldItems.isHoldingAny(player));
        assertTrue(heldItems.hold(otherPlayer, item));
    }

    @Test
    void releaseWithoutAHoldReturnsNothing() {
        assertEquals(OptionalInt.empty(), heldItems.release(player));
    }

    @Test
    void dropOfTheHeldItemIsAccepted() {
        heldItems.hold(player, item);

        assertTrue(requireHeld(item.getSerialId()));
    }

    @Test
    void dropOfAnItemThatIsNotHeldIsRefused() {
        heldItems.hold(player, item);

        assertFalse(requireHeld(otherItem.getSerialId()));
    }

    @Test
    void dropWithoutAHoldIsRefused() {
        assertFalse(requireHeld(item.getSerialId()));
    }

    private boolean requireHeld(int droppedSerial) {
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x08);
        buffer.writeInt(droppedSerial);
        buffer.writeShort(100);
        buffer.writeShort(100);
        buffer.writeByte(0);
        buffer.writeByte(0);
        buffer.writeInt(-1);
        try {
            var context = DropItemContext.ofDropItem(player, new DropItem(buffer));
            return new RequireHeldItemStep<DropItemContext>(heldItems).execute(context).flowSucceeded();
        } finally {
            buffer.release();
        }
    }

    private static UOPlayer player(int serial) {
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(serial);
        return player;
    }

    private static UOItem item(int serial) {
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(serial);
        return item;
    }
}
