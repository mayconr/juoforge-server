package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class FightOccuring extends AbstractPacket {
    public static final int CODE = (byte) 0x2F;
    private final int attackerSerialId;
    private final int opponentSerialId;

    public FightOccuring(int attackerSerialId, int opponentSerialId) {
        super(CODE, 10);
        this.attackerSerialId = attackerSerialId;
        this.opponentSerialId = opponentSerialId;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeInt(getCode());
        buf.writeByte(0);
        buf.writeInt(attackerSerialId);
        buf.writeInt(opponentSerialId);
    }
}
