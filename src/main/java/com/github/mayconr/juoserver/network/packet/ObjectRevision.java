package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class ObjectRevision extends AbstractPacket {

    public static final int CODE = (byte) 0xDC;
    private static final int BASE_HASH = 0x40000000;

    private final int serialId;
    private final int hash;

    public ObjectRevision(UOItem item) {
        super(CODE, 9);
        this.serialId = item.getSerialId();
        this.hash = BASE_HASH + RevisionUtils.itemRevisionHashCode(item);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(serialId);
        buf.writeInt(hash);
    }
}
