package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class DrawContainer extends AbstractPacket {

    public static final int CODE = (byte) 0x24;
    private Container container;

    public DrawContainer(Container container) {
        super(CODE, 9);
        this.container = container;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(container.getSerialId());
        buf.writeShort(container.getContainerGumpId());
        buf.writeShort(0x7D);
    }
}
