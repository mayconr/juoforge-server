package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class RequestHelp extends AbstractPacket {

    public static final int CODE = (byte) 0x9B;

    public RequestHelp(ByteBuf buf) {
        super(CODE, 258);
        buf.skipBytes(258);
    }

}
