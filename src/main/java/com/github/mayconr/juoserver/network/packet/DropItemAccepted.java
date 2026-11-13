package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

/** Ends the client's pending drop after successful placement. */
public final class DropItemAccepted extends AbstractPacket {
    public static final int CODE = 0x29;

    public DropItemAccepted() {
        super(CODE, 1);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
    }
}
