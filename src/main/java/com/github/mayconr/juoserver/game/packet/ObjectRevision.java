package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class ObjectRevision extends AbstractPacket {

    public static final int CODE = (byte) 0xDC;
    private static final int BASE_HASH = 0x40000000;

    private int serialId;
    private int hash;

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
