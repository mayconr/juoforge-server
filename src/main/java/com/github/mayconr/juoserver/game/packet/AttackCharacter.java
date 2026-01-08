package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class AttackCharacter extends AbstractPacket {

    public static final int CODE = (byte) 0xAA;
    private final int opponentSerialId;

    public AttackCharacter(int opponentSerialId) {
        super(CODE, 5);
        this.opponentSerialId = opponentSerialId;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(opponentSerialId);
    }
}
