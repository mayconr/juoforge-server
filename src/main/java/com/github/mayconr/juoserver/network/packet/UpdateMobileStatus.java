package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class UpdateMobileStatus extends AbstractPacket {

    public static final int CODE = 0xDE;
    private final int serialId;
    private final int attackerSerialId;

    public UpdateMobileStatus(int serialId, int attackerSerialId) {
        super(CODE, computeLength(attackerSerialId));
        this.serialId = serialId;
        this.attackerSerialId = attackerSerialId;
    }

    static int computeLength(int attackerSerialId) {
        return 8 + (attackerSerialId > 0 ? 4 : 0);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(getCode());
        buf.writeShort(getLength());
        buf.writeInt(serialId);
        buf.writeByte(attackerSerialId > 0 ? 1 : 0);
        if (attackerSerialId > 0) {
            buf.writeInt(attackerSerialId);
        }
    }
}
