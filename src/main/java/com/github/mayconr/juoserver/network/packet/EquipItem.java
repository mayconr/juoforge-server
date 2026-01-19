package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class EquipItem extends AbstractPacket {
    public static final int CODE = 0x2e;

    private final UOMobile mobile;
    private final Layer layer;
    private final UOItem item;

    public EquipItem(UOMobile mobile, Layer layer, UOItem item) {
        super(CODE, 15);
        this.mobile = mobile;
        this.layer = layer;
        this.item = item;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(item.getSerialId());
        buf.writeShort(item.getModelId() & 0xFFFF);
        buf.writeByte(0);
        buf.writeByte(layer.getCode() & 0xFF);
        buf.writeInt(mobile.getSerialId());
        buf.writeShort(item.getHue() & 0xFFFF);
    }
}
