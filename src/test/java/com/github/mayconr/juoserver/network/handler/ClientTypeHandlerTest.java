package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.ClientTypeExtendedCommand;
import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.NettyPlayerSession;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientTypeHandlerTest {
    @ParameterizedTest
    @ValueSource(longs = {0, 127, 2147483648L, 4294967295L})
    void storesUnsignedClientFlagsInTheSession(long flags) {
        var buffer = Unpooled.buffer();
        var channel = new EmbeddedChannel();
        try {
            buffer.writeByte(0xBF).writeShort(10).writeShort(0x000F);
            buffer.writeByte(0x0A).writeInt((int) flags);
            var packet = new GeneralInformation(buffer);
            var command = assertInstanceOf(ClientTypeExtendedCommand.class, packet.getCommand());
            assertEquals(0x0A, command.marker());
            assertEquals(flags, command.flags());
            assertEquals(0, buffer.readableBytes());
            var session = new NettyPlayerSession(channel, null, null, null, null, null);
            assertEquals(0, session.getClientExpansionFlags());
            new GeneralInformationHandler(null).channelRead0(session, null, packet);
            assertEquals(flags, session.getClientExpansionFlags());
        } finally {
            buffer.release();
            channel.finishAndReleaseAll();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 6})
    void rejectsInvalidPayloadLengths(int length) {
        var buffer = Unpooled.buffer();
        try {
            buffer.writeByte(0xBF).writeShort(5 + length).writeShort(0x000F).writeZero(length);
            assertThrows(CorruptedFrameException.class, () -> new GeneralInformation(buffer));
        } finally {
            buffer.release();
        }
    }
}
