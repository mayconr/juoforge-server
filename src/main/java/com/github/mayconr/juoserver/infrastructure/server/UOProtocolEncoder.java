package com.github.mayconr.juoserver.infrastructure.server;

import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mayconr.juoserver.network.packet.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class UOProtocolEncoder extends MessageToByteEncoder<Packet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UOProtocolEncoder.class);
    private static final Huffman HUFFMAN = new Huffman();

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {

        final var buf = Unpooled.buffer(msg.getLength());
        msg.writesTo(buf);

        byte[] plainBytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), plainBytes);
        final var compressed = Unpooled.wrappedBuffer(HUFFMAN.encode(plainBytes));

        out.writeBytes(compressed);

        if (LOGGER.isDebugEnabled()) {
            final var hex = HexFormat.ofDelimiter(" ");
            LOGGER.debug(
                    "Packet sent [Code: 0x{} - Content: {}] - Length {} ",
                    hex.formatHex(new byte[] {(byte) msg.getCode()}).toUpperCase(),
                    hex.formatHex(plainBytes).toUpperCase(),
                    plainBytes.length);
        }
    }
}
