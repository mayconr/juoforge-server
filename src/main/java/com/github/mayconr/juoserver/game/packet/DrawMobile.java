package com.github.mayconr.juoserver.game.packet;

import java.util.Map;

import com.github.mayconr.juoserver.game.core.model.Layer;
import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class DrawMobile extends AbstractPacket {

    public static final int CODE = (byte) 0x78;

    private final UOMobile mobile;

    public DrawMobile(UOMobile mobile) {
        super(CODE, computeLength(mobile));
        this.mobile = mobile;
    }

    private static int computeLength(UOMobile mobile) {
        int len = 0;
        for (UOItem item : mobile.getEquippedItems().values()) {
            len += 7 + (item.getHue() != 0 ? 2 : 0);
        }
        return 20 + len;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeInt(mobile.getSerialId());
        buf.writeShort(mobile.getModelId());
        buf.writeShort(mobile.getX());
        buf.writeShort(mobile.getY());
        buf.writeByte(mobile.getZ());
        buf.writeByte(mobile.getDirection().getCode() | (mobile.isRunning() ? 0x80 : 0));
        buf.writeShort(mobile.getHue());
        buf.writeByte(mobile.getStatus().getCode());
        buf.writeByte(mobile.getNotoriety().getCode());
        for (Map.Entry<Layer, UOItem> entry : mobile.getEquippedItems().entrySet()) {
            final Layer layer = entry.getKey();
            final UOItem item = entry.getValue();

            int modelId = item.getModelId() & 0x7FFF;
            boolean writeHue = item.getHue() != 0;
            if (writeHue) {
                modelId |= 0x8000;
            }
            buf.writeInt(item.getSerialId());
            buf.writeShort(modelId);
            buf.writeByte(layer.getCode());
            if (writeHue) {
                buf.writeShort(item.getHue());
            }
        }
        buf.writeByte(0); // end byte
    }
}
