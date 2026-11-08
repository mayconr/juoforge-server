package com.github.mayconr.juoserver.infrastructure.server;

import com.github.mayconr.juoserver.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class UOProtocolEncoderTest {
    @Test
    void releasesTheTemporaryBufferAndPreservesCompressedBytes() {
        var buffer = new AtomicReference<ByteBuf>();
        Packet packet = packet(buffer, false);
        var channel = new EmbeddedChannel(new UOProtocolEncoder());
        try {
            channel.writeOutbound(packet);
            assertEquals(0, buffer.get().refCnt());
            ByteBuf output = channel.readOutbound();
            try {
                byte[] bytes = new byte[output.readableBytes()];
                output.readBytes(bytes);
                assertArrayEquals(new Huffman().encode(new byte[]{0x73, 1}), bytes);
            } finally {
                output.release();
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void releasesTheTemporaryBufferWhenSerializationFails() {
        var buffer = new AtomicReference<ByteBuf>();
        Packet packet = packet(buffer, true);
        var channel = new EmbeddedChannel(new UOProtocolEncoder());
        try {
            assertThrows(EncoderException.class, () -> channel.writeOutbound(packet));
            assertEquals(0, buffer.get().refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private Packet packet(AtomicReference<ByteBuf> buffer, boolean fail) {
        var packet = mock(Packet.class);
        when(packet.getLength()).thenReturn(2);
        when(packet.getCode()).thenReturn(0x73);
        doAnswer(invocation -> {
            ByteBuf target = invocation.getArgument(0);
            buffer.set(target);
            target.writeByte(0x73).writeByte(1);
            if (fail) throw new IllegalStateException("Serialization failed");
            return null;
        }).when(packet).writesTo(any(ByteBuf.class));
        return packet;
    }
}
