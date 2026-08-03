package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

public final class UnknownExtendedCommand implements ExtendedCommand {

    private final byte[] payload;

    public UnknownExtendedCommand(ByteBuf buf, int length) {
        this.payload = new byte[buf.readableBytes()];
        buf.readBytes(payload);
    }

    public byte[] payload() {
        return payload;
    }

}
