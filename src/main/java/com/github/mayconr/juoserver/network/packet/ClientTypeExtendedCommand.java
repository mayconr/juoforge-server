package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

/** Client-reported expansion flags from General Information 0x000F. */
public final class ClientTypeExtendedCommand implements ExtendedCommand {
    public static final int SUB_COMMAND = 0x000F;

    private final int marker;
    private final long flags;

    public ClientTypeExtendedCommand(ByteBuf buffer) {
        if (buffer.readableBytes() != 5) {
            throw new CorruptedFrameException("Client type payload must contain 5 bytes");
        }
        marker = buffer.readUnsignedByte();
        flags = buffer.readUnsignedInt();
    }

    public int marker() {
        return marker;
    }

    public long flags() {
        return flags;
    }
}
