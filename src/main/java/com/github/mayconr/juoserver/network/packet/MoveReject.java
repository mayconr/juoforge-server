package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class MoveReject extends AbstractPacket {

    public static final int CODE = (byte) 0x21;
    private final int sequence;
    private final UOMobile mobile;

    public MoveReject(int sequence, UOMobile mobile) {
        super(CODE, 8);
        this.sequence = sequence;
        this.mobile = mobile;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        // sequence
        buf.writeByte(sequence & 0xFF);
        // xLoc
        buf.writeShort(mobile.getX());
        // yLoc
        buf.writeShort(mobile.getY());
        // direction
        buf.writeByte(mobile.getDirection().getCode() & 0x07);
        // zLoc
        buf.writeByte(mobile.getZ());
    }
}
