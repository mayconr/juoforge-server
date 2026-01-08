package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class DragItem extends AbstractPacket {
    public static final int CODE = (byte) 0x23;
    private UOMobile droppingMobile;
    private UOItem item;

    public DragItem(UOMobile droppingMobile, UOItem item) {
        super(CODE, 26);
        this.droppingMobile = droppingMobile;
        this.item = item;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(item.getModelId());
        buf.writeByte(0); // unknown
        buf.writeShort(0); // hue
        buf.writeShort(2); // stack count
        buf.writeInt(droppingMobile.getSerialId()); // source
        buf.writeShort(droppingMobile.getX());
        buf.writeShort(droppingMobile.getY());
        buf.writeByte(droppingMobile.getZ());
        buf.writeInt(0); // target id zero means no parent
        buf.writeShort(item.getX());
        buf.writeShort(item.getY());
        buf.writeByte(item.getZ());
    }
}
