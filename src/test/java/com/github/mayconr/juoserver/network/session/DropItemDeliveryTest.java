package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemDropAccepted;
import com.github.mayconr.juoserver.game.model.event.ItemDropRejected;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.AddItemToContainer;
import com.github.mayconr.juoserver.network.packet.DropItemAccepted;
import com.github.mayconr.juoserver.network.packet.EquipItem;
import com.github.mayconr.juoserver.network.packet.LiftReject;
import com.github.mayconr.juoserver.network.packet.ObjectInfo;
import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DropItemDeliveryTest {
    private final SessionView session = mock(SessionView.class);
    private final SessionPacketWriter writer = mock(SessionPacketWriter.class);
    private final WorldInternal world = mock(WorldInternal.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private final ItemEventHandler handler = new ItemEventHandler(session, writer, world);

    @BeforeEach
    void setUp() {
        when(player.getSerialId()).thenReturn(1);
    }

    @Test
    void successSendsSingleByteAcknowledgementOnlyToDroppingPlayer() {
        handler.onItemDropAccepted(new ItemDropAccepted(player));
        verifyNoInteractions(writer);
        when(session.isPlayer(player)).thenReturn(true);

        handler.onItemDropAccepted(new ItemDropAccepted(player));

        var packet = ArgumentCaptor.forClass(DropItemAccepted.class);
        verify(writer).writeAndFlush(packet.capture());
        var buffer = Unpooled.buffer();
        try {
            packet.getValue().writesTo(buffer);
            assertEquals(1, packet.getValue().getLength());
            assertEquals(1, buffer.readableBytes());
            assertEquals(0x29, buffer.readUnsignedByte());
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectionWithoutAnItemSendsOnlyDenialToDroppingPlayer() {
        var event = new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, null);
        handler.onItemDropRejected(event);
        verifyNoInteractions(writer);
        when(session.isPlayer(player)).thenReturn(true);

        handler.onItemDropRejected(event);

        var packet = ArgumentCaptor.forClass(LiftReject.class);
        verify(writer).writeAndFlush(packet.capture());
        var buffer = Unpooled.buffer();
        try {
            packet.getValue().writesTo(buffer);
            assertEquals(2, buffer.readableBytes());
            assertEquals(0x27, buffer.readUnsignedByte());
            assertEquals(LiftRejectReason.INSPECIFIC.code(), buffer.readUnsignedByte());
        } finally {
            buffer.release();
        }
    }

    @Test
    void groundRestorationFollowsDenial() {
        var item = new UOItem(data(), null);
        when(session.isPlayer(player)).thenReturn(true);
        when(session.shouldReceiveUpdate(item)).thenReturn(true);

        handler.onItemDropRejected(new ItemDropRejected(player, LiftRejectReason.OUT_OF_RANGE, item));

        var order = inOrder(writer);
        order.verify(writer).writeAndFlush(isA(LiftReject.class));
        order.verify(writer).writeAndFlush(isA(ObjectInfo.class));
        order.verifyNoMoreInteractions();
    }

    @Test
    void containerRestorationFollowsDenial() {
        var item = new UOItem(data(), null);
        var containerData = data();
        containerData.setSerialId(0x40000002);
        var container = new UOContainer(containerData, null);
        item.setCurrentLocation(ItemLocation.container(container.getSerialId()));
        var items = mock(ItemModule.class);
        when(world.item()).thenReturn(items);
        when(items.getItemBySerialId(container.getSerialId())).thenReturn(Optional.of(container));
        when(session.isPlayer(player)).thenReturn(true);
        when(session.shouldReceiveUpdate(container)).thenReturn(true);

        handler.onItemDropRejected(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, item));

        var order = inOrder(writer);
        order.verify(writer).writeAndFlush(isA(LiftReject.class));
        order.verify(writer).writeAndFlush(isA(AddItemToContainer.class));
        order.verifyNoMoreInteractions();
    }

    @Test
    void equippedRestorationFollowsDenial() {
        var item = new UOItem(data(), null);
        item.setLayer(Layer.ONE_HANDED);
        item.setCurrentLocation(ItemLocation.equipped(1));
        when(session.isPlayer(player)).thenReturn(true);
        when(session.shouldReceiveUpdate(player)).thenReturn(true);

        handler.onItemDropRejected(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, item));

        var order = inOrder(writer);
        order.verify(writer).writeAndFlush(isA(LiftReject.class));
        order.verify(writer).writeAndFlush(isA(EquipItem.class));
        order.verifyNoMoreInteractions();
    }

    @Test
    void nearbyObserverReceivesRestoredItemWithoutDenial() {
        var item = new UOItem(data(), null);
        when(session.shouldReceiveUpdate(item)).thenReturn(true);

        handler.onItemDropRejected(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC, item));

        verify(writer).writeAndFlush(isA(ObjectInfo.class));
        verifyNoMoreInteractions(writer);
    }

    @Test
    void distantObserverReceivesNothing() {
        handler.onItemDropRejected(new ItemDropRejected(player, LiftRejectReason.INSPECIFIC,
                new UOItem(data(), null)));
        verifyNoInteractions(writer);
    }

    private static UOItemData data() {
        var data = new UOItemData();
        data.setSerialId(0x40000001);
        data.setAmount(1);
        data.setFlags(List.of());
        data.setLocationType(ItemLocationType.GROUND);
        return data;
    }
}
