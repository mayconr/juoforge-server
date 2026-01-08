package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class LoginComplete extends AbstractPacket {

    public static final int CODE = (byte) 0x55;

    public LoginComplete() {
        super(CODE, 1);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
    }
}
