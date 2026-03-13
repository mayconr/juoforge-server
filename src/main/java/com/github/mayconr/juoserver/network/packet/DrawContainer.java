package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class DrawContainer extends AbstractPacket {

    public static final int CODE = (byte) 0x24;
    private final int serialId;
    private final int gumpId;

    public DrawContainer(Container container) {
        this(container.getSerialId(), container.getContainerGumpId());
    }

    public DrawContainer(int serialId, int gumpId) {
        super(CODE, 7);
        this.serialId = serialId;
        this.gumpId = gumpId;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(serialId);
        buf.writeShort(gumpId);
        buf.writeShort(0x7D);
    }
}
