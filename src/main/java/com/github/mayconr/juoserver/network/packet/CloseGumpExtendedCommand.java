package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

/** Closes a server gump without asking the client to submit a button response. */
public record CloseGumpExtendedCommand(int gumpId) implements OutboundExtendedCommand {
    public static final int SUB_COMMAND = 0x0004;

    @Override
    public int subCommand() {
        return SUB_COMMAND;
    }

    @Override
    public int payloadLength() {
        return 8;
    }

    @Override
    public void writesTo(ByteBuf buffer) {
        buffer.writeInt(gumpId);
        buffer.writeInt(0); // Close directly; nonzero values trigger OnButtonClick in ClassicUO.
    }
}
