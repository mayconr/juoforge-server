package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

/** Client request for the context menu of an item or mobile (0xBF/0x0013). */
public record ContextMenuRequestExtendedCommand(int serialId) implements ExtendedCommand {
    public static final int SUB_COMMAND = 0x0013;

    public ContextMenuRequestExtendedCommand(ByteBuf buffer) {
        this(readSerial(buffer));
    }

    private static int readSerial(ByteBuf buffer) {
        if (buffer.readableBytes() != Integer.BYTES) {
            throw new IllegalArgumentException("Context menu request must contain exactly one serial");
        }
        return buffer.readInt();
    }
}
