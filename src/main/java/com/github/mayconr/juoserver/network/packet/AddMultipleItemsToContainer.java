package com.github.mayconr.juoserver.network.packet;

import java.util.Collection;

import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class AddMultipleItemsToContainer extends AbstractPacket {
    public static final int CODE = (byte) 0x3C;
    private final Container container;
    private final Collection<UOItem> items;

    public AddMultipleItemsToContainer(Container container, Collection<UOItem> items) {
        super(CODE, computeLength(items));
        this.container = container;
        this.items = items;
    }

    private static int computeLength(Collection<UOItem> items) {
        return 5 + items.size() * 20;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeShort(items.size());
        for (UOItem item : items) {
            buf.writeInt(item.getSerialId());
            buf.writeShort(item.getModelId());
            buf.writeByte(0); // unknown
            buf.writeShort(item.getAmount());
            buf.writeShort(item.getX());
            buf.writeShort(item.getY());
            buf.writeByte(item.getZ());
            buf.writeInt(container.getSerialId());
            buf.writeShort(item.getHue());
        }
    }
}
