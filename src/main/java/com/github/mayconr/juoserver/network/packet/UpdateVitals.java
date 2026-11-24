package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.mobile.VitalType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class UpdateVitals extends AbstractPacket {
    private final int serial;
    private final int max;
    private final int value;

    public UpdateVitals(VitalType type, int serial, int max, int value) {
        super(opcode(type), 9);
        this.serial = serial;
        this.max = max;
        this.value = value;
    }

    private static int opcode(VitalType type) {
        return switch (type) {
            case HITPOINTS -> UpdateHealth.CODE;
            case MANA -> (byte) 0xA2;
            case STAMINA -> (byte) 0xA3;
        };
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(getCode());
        buf.writeInt(serial);
        buf.writeShort(max);
        buf.writeShort(value);
    }
}
