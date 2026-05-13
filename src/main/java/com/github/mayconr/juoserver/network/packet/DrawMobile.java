package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.Map;

public class DrawMobile extends AbstractPacket {

    public static final int CODE = (byte) 0x78;

    private final UOMobile mobile;
    private final Map<Layer, UOItem> equippedItems;

    public DrawMobile(UOMobile mobile, Map<Layer, UOItem> equippedItems) {
        super(CODE, computeLength(equippedItems.values()));
        this.mobile = mobile;
        this.equippedItems = equippedItems;
    }

    private static int computeLength(Collection<UOItem> equippedItems) {
        int len = 0;
        for (UOItem item : equippedItems) {
            len += 7 + (item.getHue() != 0 ? 2 : 0);
        }
        return 19 + len + 4;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());

        boolean isDead = mobile instanceof UOPlayer player && !player.isAlive();

        int serial = mobile.getSerialId();
        if (isDead) {
            serial = serial | 0x80000000;
        }
        buf.writeInt(serial);

        int modelId = mobile.getModelId();
        if (mobile instanceof  UOPlayer player && isDead) {
            modelId = player.getGhostModelId();
        }
        buf.writeShort(modelId);

        buf.writeShort(mobile.getX());
        buf.writeShort(mobile.getY());
        buf.writeByte(mobile.getZ());
        buf.writeByte(mobile.getDirection().getCode());

        int hue = mobile.getHue();
        if (isDead) {
            hue = 0;
        }
        buf.writeShort(hue);

        buf.writeByte(mobile.getStatus().getCode());
        buf.writeByte(mobile.getNotoriety().getCode());

        for (Map.Entry<Layer, UOItem> entry : equippedItems.entrySet()) {
            final Layer layer = entry.getKey();
            final UOItem item = entry.getValue();

            int itemModelId = item.getModelId() & 0x7FFF;
            boolean writeHue = item.getHue() != 0;
            if (writeHue) {
                itemModelId |= 0x8000;
            }

            buf.writeInt(item.getSerialId());
            buf.writeShort(itemModelId);
            buf.writeByte(layer.getCode());
            if (writeHue) {
                buf.writeShort(item.getHue());
            }
        }
        buf.writeInt(0); // end byte
    }
}
