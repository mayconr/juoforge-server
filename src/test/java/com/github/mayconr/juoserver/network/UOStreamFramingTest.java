package com.github.mayconr.juoserver.network;

import com.github.mayconr.juoserver.infrastructure.server.UOProtocolDecoder;
import com.github.mayconr.juoserver.network.packet.ClientVersion;
import com.github.mayconr.juoserver.network.packet.GameServerLogin;
import com.github.mayconr.juoserver.network.packet.PingPong;
import com.github.mayconr.juoserver.network.packet.OpenChatRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class UOStreamFramingTest {
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "José", "123456789012345678901234567890"})
    void decodesOpenChatWithoutDiscardingTheFollowingPacket(String name) {
        var channel = new EmbeddedChannel(new UOProtocolDecoder());
        var stream = Unpooled.buffer();
        try {
            stream.writeInt(0x12345678);
            stream.writeByte(0xB5).writeByte(0);
            stream.writeCharSequence(name, StandardCharsets.UTF_16BE);
            stream.writeZero(62 - name.length() * 2);
            stream.writeByte(0x73).writeByte(7);
            while (stream.isReadable()) channel.writeInbound(stream.readRetainedSlice(1));
            var chat = assertInstanceOf(OpenChatRequest.class, channel.readInbound());
            assertEquals(name, chat.getName());
            assertEquals(64, chat.getLength());
            assertEquals(7, ((PingPong) channel.readInbound()).getSequence());
            assertNull(channel.readInbound());

            var handlerChannel = new EmbeddedChannel(
                    new com.github.mayconr.juoserver.network.handler.OpenChatRequestHandler());
            try {
                assertFalse(handlerChannel.writeInbound(chat));
            } finally {
                handlerChannel.finishAndReleaseAll();
            }
        } finally {
            stream.release();
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void reassemblesSecondLoginAndVariableLengthPacketOneByteAtATime() {
        EmbeddedChannel channel = new EmbeddedChannel(new UOProtocolDecoder());
        ByteBuf stream = Unpooled.buffer();
        try {
            stream.writeInt(0x12345678).writeByte(0x91).writeInt(0x12345678);
            stream.writeBytes("account".getBytes(StandardCharsets.US_ASCII)).writeZero(23);
            stream.writeBytes("password".getBytes(StandardCharsets.US_ASCII)).writeZero(22);
            stream.writeByte(0xBD).writeShort(11).writeBytes("7.0.0.0\0".getBytes(StandardCharsets.US_ASCII));
            stream.writeByte(0x73).writeByte(7);
            while (stream.isReadable()) channel.writeInbound(stream.readRetainedSlice(1));
            GameServerLogin login = channel.readInbound();
            assertEquals("account", login.getUsername());
            assertEquals("password", login.getPassword());
            assertEquals(0x12345678, login.getKey());
            ClientVersion version = channel.readInbound();
            assertEquals("7.0.0.0", version.getClientVersion());
            PingPong ping = channel.readInbound();
            assertEquals(7, ping.getSequence());
            assertNull(channel.readInbound());
        } finally {
            stream.release();
            channel.finishAndReleaseAll();
        }
    }
}
