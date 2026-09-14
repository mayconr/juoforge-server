package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

/**
 * Let the wallpaper gray when hitpoints = 0
 */
public class UpdateHealth extends AbstractPacket {

    private final UOMobile mobile;

    public static final int CODE = (byte) 0xA1;
    public UpdateHealth(UOMobile mobile) {
        super(CODE, 9);
        this.mobile = mobile;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(getCode());
        buf.writeInt(mobile.getSerialId());
        buf.writeShort(mobile.getMaxHitpoints());
        buf.writeShort(mobile.getHitpoints());
    }
}
