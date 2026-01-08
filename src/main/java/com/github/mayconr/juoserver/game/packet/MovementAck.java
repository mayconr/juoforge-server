package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.Notoriety;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class MovementAck extends AbstractPacket {

    public static final int CODE = (byte) 0x22;

    private int sequence;
    private Notoriety notoriety;

    public MovementAck(int sequence, Notoriety notoriety) {
        super(CODE, 3);
        this.sequence = sequence;
        this.notoriety = notoriety;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(sequence);
        buf.writeByte(notoriety.getCode());
    }
}
