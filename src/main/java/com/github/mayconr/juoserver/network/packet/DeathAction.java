package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class DeathAction extends AbstractPacket {

    public static final int CODE = 0xAF;

    private final UOMobile mobile;
    private final int corpseSerial;

    public DeathAction(UOMobile mobile, int corpseSerial) {
        super(CODE, 13);
        this.mobile = mobile;
        this.corpseSerial = corpseSerial;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(mobile.getSerialId());
        buf.writeInt(corpseSerial);
        buf.writeInt(0); // unknown
    }
}
