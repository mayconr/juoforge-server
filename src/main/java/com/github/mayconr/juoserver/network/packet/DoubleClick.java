package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DoubleClick extends AbstractPacket {

    public static final int CODE = (byte) 0x06;
    private final int serialId;
    private final boolean paperdool;

    public DoubleClick(ByteBuf buf) {
        super(CODE, 5);
        buf.readByte(); // code
        final int rawSerialId = buf.readInt();
        this.paperdool = (rawSerialId & 0x80000000) != 0;
        this.serialId = rawSerialId & 0x7FFFFFFF;
    }
}
