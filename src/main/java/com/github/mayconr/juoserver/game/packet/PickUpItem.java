package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PickUpItem extends AbstractPacket {
    public static final int CODE = (byte) 0x07;

    private final int serialId;
    private final int amount;

    public PickUpItem(ByteBuf buf) {
        super(CODE, 7);
        buf.readByte(); // CODE
        this.serialId = buf.readInt();
        this.amount = buf.readShort();
    }
}
