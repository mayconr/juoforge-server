package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.ui.UIModule;
import com.github.mayconr.juoserver.game.ui.UIModuleImpl;
import com.github.mayconr.juoserver.game.model.event.ObjectLabelRequested;
import com.github.mayconr.juoserver.game.ui.flow.singleclick.*;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.SendSpeech;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import com.github.mayconr.juoserver.network.session.SessionPacketWriter;
import com.github.mayconr.juoserver.network.session.SessionView;
import com.github.mayconr.juoserver.network.session.handler.PlayerInterfaceHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SingleClickFlowTest {
    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    void deliversObjectLabelOnlyToTheRequestingSession(boolean mobile, boolean exists) {
        var storage = mock(RealmStorage.class);
        var events = new DefaultEventBus();
        var player = mock(UOPlayer.class);
        var observer = mock(UOPlayer.class);
        int serial = mobile ? 42 : 0x40000000;
        UOObject<?> object;
        if (mobile) {
            var data = new UOMobileData();
            data.setSerialId(serial);
            data.setModelId(400);
            var npc = new UONpc(data);
            when(storage.getMobile(serial)).thenReturn(exists ? Optional.of(npc) : Optional.empty());
            object = npc;
        } else {
            var data = new UOItemData();
            data.setSerialId(serial);
            data.setModelId(5049);
            data.setLocationType(ItemLocationType.GROUND);
            var item = new UOItem(data, null);
            when(storage.getItem(serial)).thenReturn(exists ? Optional.of(item) : Optional.empty());
            object = item;
        }
        object.setName("internal");
        object.setDisplayName("Visible name");
        var requesterChannel = new EmbeddedChannel();
        var observerChannel = new EmbeddedChannel();
        var requestBytes = Unpooled.buffer().writeByte(0x09).writeInt(serial);
        var encoded = Unpooled.buffer();
        try {
            var requester = new PlayerInterfaceHandler(new SessionView(() -> player, () -> null, null),
                    new SessionPacketWriter(requesterChannel, null), null);
            var other = new PlayerInterfaceHandler(new SessionView(() -> observer, () -> null, null),
                    new SessionPacketWriter(observerChannel, null), null);
            events.register(ObjectLabelRequested.class, requester::onObjectLabelRequested);
            events.register(ObjectLabelRequested.class, other::onObjectLabelRequested);
            var registry = new DefaultFlowRegistry();
            registry.register("single-click", SingleClickFlowDefinition.build(storage, events), SingleClickContext.class);
            UIModule ui = new UIModuleImpl(null, null, null);
            ui.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));

            ui.singleClick(player, new SingleClickRequest(requestBytes));
            requesterChannel.runPendingTasks();
            observerChannel.runPendingTasks();

            assertNull(observerChannel.readOutbound());
            if (exists) {
                var packet = assertInstanceOf(SendSpeech.class, requesterChannel.readOutbound());
                packet.writesTo(encoded);
                assertEquals(0x1C, encoded.getUnsignedByte(0));
                assertEquals(encoded.readableBytes(), encoded.getUnsignedShort(1));
                assertEquals(serial, encoded.getInt(3));
                assertEquals(object.getModelId(), encoded.getUnsignedShort(7));
                assertEquals(0x06, encoded.getUnsignedByte(9));
                assertEquals("Visible name", encoded.toString(44, encoded.readableBytes() - 45, StandardCharsets.US_ASCII));
                assertEquals(0, encoded.getUnsignedByte(encoded.writerIndex() - 1));
            }
            assertNull(requesterChannel.readOutbound());
        } finally {
            requestBytes.release();
            encoded.release();
            requesterChannel.finishAndReleaseAll();
            observerChannel.finishAndReleaseAll();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 0x60000000})
    void ignoresInvalidAndVirtualSerials(int serial) {
        var storage = mock(RealmStorage.class);
        var events = mock(com.github.mayconr.juoserver.infrastructure.eventbus.EventBus.class);
        var buffer = Unpooled.buffer().writeByte(0x09).writeInt(serial);
        try {
            SingleClickFlowDefinition.build(storage, events)
                    .execute(new SingleClickContext(mock(UOPlayer.class), new SingleClickRequest(buffer)));
            verifyNoInteractions(storage, events);
        } finally {
            buffer.release();
        }
    }
}
