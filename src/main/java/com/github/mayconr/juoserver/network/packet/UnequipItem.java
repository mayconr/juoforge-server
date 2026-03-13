package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnequipItem extends AbstractPacket {
    public static final int CODE = (byte) 0x07;

    private final int serialId;
    private final int amount;

    public UnequipItem(ByteBuf buf) {
        super(CODE, 7);
        buf.readByte(); // CODE
        this.serialId = buf.readInt();
        this.amount = buf.readShort();
    }
}
