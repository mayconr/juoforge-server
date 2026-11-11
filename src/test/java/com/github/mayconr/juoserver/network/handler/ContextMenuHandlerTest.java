package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.ContextMenuRequestExtendedCommand;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuHandlerTest {
    @Test
    void decodesClientRequestAndReturnsMockMenuUsingSameSerial() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf0009001340000032"));
        var responseBytes = Unpooled.buffer();
        try {
            var packet = new GeneralInformation(buffer);
            var request = assertInstanceOf(ContextMenuRequestExtendedCommand.class, packet.getCommand());
            assertEquals(0x40000032, request.serialId());
            assertEquals(0, buffer.readableBytes());
            var session = mock(PlayerSession.class);
            when(session.getPlayer()).thenReturn(mock(UOPlayer.class));
            var ctx = mock(ChannelHandlerContext.class);
            var handler = new GeneralInformationHandler(mock(WorldInternal.class));

            handler.channelRead0(session, ctx, packet);

            var response = ArgumentCaptor.forClass(GeneralInformation.class);
            verify(ctx).writeAndFlush(response.capture());
            response.getValue().writesTo(responseBytes);
            // Header, mode 1, same serial, one entry: index 0, cliloc offset 6123, flags 0.
            assertEquals("bf0012001400014000003201000017eb0000",
                    HexFormat.of().formatHex(toBytes(responseBytes)));
        } finally {
            buffer.release();
            responseBytes.release();
        }
    }

    @Test
    void doesNotReplyWithoutAnActivePlayer() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf0009001340000032"));
        try {
            var ctx = mock(ChannelHandlerContext.class);
            new GeneralInformationHandler(mock(WorldInternal.class))
                    .channelRead0(mock(PlayerSession.class), ctx, new GeneralInformation(buffer));
            verifyNoInteractions(ctx);
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsRequestWithIncompleteSerial() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf00080013400000"));
        try {
            assertThrows(IllegalArgumentException.class, () -> new GeneralInformation(buffer));
        } finally {
            buffer.release();
        }
    }

    private byte[] toBytes(ByteBuf buffer) {
        var bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }
}
