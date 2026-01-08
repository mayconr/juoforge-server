package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class LookRequest extends AbstractPacket {
    public static final int CODE = (byte) 0x09;

    private final int serialId;

    public LookRequest(ByteBuf buf) {
        super(CODE, 5);
        buf.readByte(); // CODE
        this.serialId = buf.readInt();
    }

    public int getSerialId() {
        return serialId;
    }
}
