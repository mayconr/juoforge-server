package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public class PingPong extends AbstractPacket {
    public static final int CODE = (byte) 0x73;
    private final int sequence;

    public PingPong(int sequence) {
        super(CODE, 2);
        this.sequence = sequence;
    }

    public PingPong(ByteBuf buf) {
        super(CODE, 2);
        buf.readByte(); // ignore code
        this.sequence = buf.readByte();
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(sequence);
    }

    public int getSequence() {
        return sequence;
    }
}
