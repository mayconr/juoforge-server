package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class RequestHelp extends AbstractPacket {

    public static final int CODE = (byte) 0x9B;

    public RequestHelp(ByteBuf buf) {
        super(CODE, 258);
        buf.readByte(); // CODE
        byte[] noData = new byte[257];
        buf.readBytes(noData);
    }
}
