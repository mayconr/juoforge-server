package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class CombatantChange extends AbstractPacket {

    public static final int CODE = (byte) 0xDE;
    private final UOMobile target;
    private final UOMobile attacker;

    public CombatantChange(UOMobile target, UOMobile attacker) {
        super(CODE, computeLength(attacker!=null));
        this.target = target;
        this.attacker = attacker;
    }

    static int computeLength(boolean hasAttacker) {
        return 8 + (hasAttacker ? 4 : 0);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(getCode());
        buf.writeShort(getLength());
        buf.writeInt(target.getSerialId());
        buf.writeByte(attacker != null ? 1 : 0);
        if (attacker != null) {
            buf.writeInt(attacker.getSerialId());
        }
    }
}
