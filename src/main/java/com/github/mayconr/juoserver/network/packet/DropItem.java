package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class DropItem extends AbstractPacket implements Location {

    public static final int CODE = (byte) 0x08;
    private final int serialId;
    private final int x;
    private final int y;
    private final int z;
    private final int containerGridIndex;
    private final int containerSerialId;

    public DropItem(ByteBuf buf) {
        super(CODE, 15);
        buf.readByte(); // CODE
        this.serialId = buf.readInt();
        this.x = buf.readShort();
        this.y = buf.readShort();
        this.z = buf.readByte();
        this.containerGridIndex = buf.readByte();
        this.containerSerialId = buf.readInt();
    }

    public boolean isContainerDrop() {
        return containerSerialId > -1;
    }
}
