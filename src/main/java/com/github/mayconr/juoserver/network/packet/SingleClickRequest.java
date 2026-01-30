package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class SingleClickRequest extends AbstractPacket {
    public static final int CODE = (byte) 0x09;

    private final int serialId;

    public SingleClickRequest(ByteBuf buf) {
        super(CODE, 5);
        buf.readByte(); // CODE
        this.serialId = buf.readInt();
    }

    public int getSerialId() {
        return serialId;
    }
}
