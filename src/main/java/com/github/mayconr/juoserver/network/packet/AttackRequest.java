package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class AttackRequest extends AbstractPacket {

    public static final int CODE = (byte) 0x05;
    private final int opponentSerialId;

    public AttackRequest(ByteBuf buf) {
        super(CODE, 5);
        buf.readByte();
        this.opponentSerialId = buf.readInt();
    }
}
