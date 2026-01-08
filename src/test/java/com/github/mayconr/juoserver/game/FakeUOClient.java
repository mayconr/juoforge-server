package com.github.mayconr.juoserver.game;

import java.util.Objects;

import com.github.mayconr.juoserver.game.packet.Packet;
import com.github.mayconr.juoserver.game.server.Huffman;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;

public class FakeUOClient {
    private final EmbeddedChannel channel;

    public FakeUOClient(ChannelInitializer<Channel> initializer) {
        this.channel = new EmbeddedChannel(initializer);
    }

    /* =========================
    Envio de packets (INBOUND)
    ========================= */

    public FakeUOClient send(Packet packet) {
        channel.writeInbound(packet);
        flush();
        return this;
    }

    /* =========================
    Recebimento (OUTBOUND)
    ========================= */

    public ByteBuf receiveRaw() {
        ByteBuf result = null;

        Object msg;
        while ((msg = channel.readOutbound()) != null) {

            if (!(msg instanceof ByteBuf buf)) {
                throw new IllegalStateException("Outbound não é ByteBuf: " + msg.getClass());
            }

            if (result == null) {
                result = buf.alloc().buffer();
            }

            result.writeBytes(buf);
            buf.release();
        }

        return decodeHuffman(Objects.requireNonNull(result), result.alloc());
    }

    private ByteBuf decodeHuffman(ByteBuf compressed, ByteBufAllocator alloc) {
        byte[] in = new byte[compressed.readableBytes()];
        compressed.getBytes(compressed.readerIndex(), in);

        byte[] decoded = Huffman.decode(in);

        return Unpooled.wrappedBuffer(decoded);
    }

    /* =========================
    Execução de tasks
    ========================= */

    public void flush() {
        channel.runPendingTasks();
        channel.runScheduledPendingTasks();
    }

    public EmbeddedChannel channel() {
        return channel;
    }
}
