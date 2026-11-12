package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.UIModule;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.ContextMenuRequestExtendedCommand;
import com.github.mayconr.juoserver.network.packet.ContextMenuSelectionExtendedCommand;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ContextMenuHandlerTest {
    @Test
    void decodesClientRequestAndDelegatesToUi() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf0009001340000032"));
        try {
            var packet = new GeneralInformation(buffer);
            var request = assertInstanceOf(ContextMenuRequestExtendedCommand.class, packet.getCommand());
            assertEquals(0x40000032, request.serialId());
            assertEquals(0, buffer.readableBytes());
            var player = mock(UOPlayer.class);
            var session = mock(PlayerSession.class);
            when(session.getPlayer()).thenReturn(player);
            var world = mock(WorldInternal.class);
            var ui = mock(UIModule.class);
            when(world.ui()).thenReturn(ui);
            var ctx = mock(ChannelHandlerContext.class);

            new GeneralInformationHandler(world).channelRead0(session, ctx, packet);

            verify(ui).requestContextMenu(player, request.serialId());
            verifyNoInteractions(ctx);
        } finally {
            buffer.release();
        }
    }

    @Test
    void decodesClassicUoSelectionAndDelegatesToUi() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf000b001540000032ffff"));
        try {
            var packet = new GeneralInformation(buffer);
            var selection = assertInstanceOf(ContextMenuSelectionExtendedCommand.class, packet.getCommand());
            assertEquals(0x40000032, selection.serialId());
            assertEquals(65535, selection.index());
            assertEquals(0, buffer.readableBytes());
            var session = mock(PlayerSession.class);
            var player = mock(UOPlayer.class);
            when(session.getPlayer()).thenReturn(player);
            var world = mock(WorldInternal.class);
            var ui = mock(UIModule.class);
            when(world.ui()).thenReturn(ui);
            var ctx = mock(ChannelHandlerContext.class);

            new GeneralInformationHandler(world).channelRead0(session, ctx, packet);

            verify(ui).onContextMenuSelection(player, 0x40000032, 65535);
            verifyNoInteractions(ctx);
        } finally {
            buffer.release();
        }
    }

    @Test
    void rejectsTruncatedAndTrailingSelectionPayloads() {
        for (var hex : new String[]{"bf000a001540000032ff", "bf000c001540000032ffff00"}) {
            var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex(hex));
            try {
                assertThrows(IllegalArgumentException.class, () -> new GeneralInformation(buffer));
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void doesNotReplyWithoutAnActivePlayer() {
        var buffer = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf0009001340000032"));
        try {
            var ctx = mock(ChannelHandlerContext.class);
            new GeneralInformationHandler(mock(WorldInternal.class, RETURNS_DEEP_STUBS))
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

}
