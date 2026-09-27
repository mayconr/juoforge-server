package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

/**
 * Client notification that the status window for a character was closed.
 */
public record CloseStatusExtendedCommand(int serialId) implements ExtendedCommand {

    public static final int SUB_COMMAND = 0x000C;

    public CloseStatusExtendedCommand(ByteBuf buffer) {
        this(buffer.readInt());
    }
}
