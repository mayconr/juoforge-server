package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

/** Client popup selection: target serial followed by an unsigned entry index (0xBF/0x0015). */
public record ContextMenuSelectionExtendedCommand(int serialId, int index) implements ExtendedCommand {
    public static final int SUB_COMMAND = 0x0015;

    public ContextMenuSelectionExtendedCommand(ByteBuf buffer) {
        this(readSerial(buffer), buffer.readUnsignedShort());
    }

    private static int readSerial(ByteBuf buffer) {
        if (buffer.readableBytes() != Integer.BYTES + Short.BYTES) {
            throw new IllegalArgumentException("Context menu selection must contain a serial and entry index");
        }
        return buffer.readInt();
    }
}
