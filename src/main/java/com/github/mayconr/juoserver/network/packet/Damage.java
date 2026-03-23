package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class Damage extends AbstractPacket {

    public static final int CODE = (byte) 0x0B;

    private final UOMobile mobile;
    private final int damage;

    public Damage(UOMobile mobile, int damage) {
        super(CODE, 7);
        this.mobile = mobile;
        this.damage = damage;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(mobile.getSerialId());
        buf.writeShort(Math.min(damage, 0xFFFF) & 0xFFFF);
    }
}
