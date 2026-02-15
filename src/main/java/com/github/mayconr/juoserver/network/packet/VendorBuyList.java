package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.event.BuyGumpSent;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VendorBuyList extends AbstractPacket {
    public static final int CODE = (byte) 0x74;

    private final UOObject container;
    private final List<BuyGumpSent.StockItem> items;

    public VendorBuyList(UOObject container, List<BuyGumpSent.StockItem> items) {
        super(CODE, calculateLength(items));
        this.container = container;
        this.items = new ArrayList<>(items.size());
        for (int i = items.size() - 1; i >= 0; i--) {
            this.items.add(items.get(i));
        }
    }

    private static int calculateLength(List<BuyGumpSent.StockItem> items) {
        int length = 8; // cmd + size + containerSerial + count

        for (BuyGumpSent.StockItem item : items) {
            byte[] nameBytes = item.entry().getItemTemplate().displayName().getBytes(StandardCharsets.US_ASCII);

            if (nameBytes.length > 255) {
                throw new IllegalArgumentException("Item name too long");
            }

            length += 5; // price + nameLength
            length += nameBytes.length;
        }

        return length;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        if (items.size() > 255) {
            throw new IllegalArgumentException("Too many items in buy list");
        }

        buf.writeByte(CODE);
        buf.writeShort(getLength());

        buf.writeInt(container.getSerialId());
        buf.writeByte(items.size());

        for (BuyGumpSent.StockItem item : items) {
            byte[] nameBytes = item.entry().getItemTemplate().displayName().getBytes(StandardCharsets.US_ASCII);

            buf.writeInt((int) item.price()); // price
            buf.writeByte(nameBytes.length);
            buf.writeBytes(nameBytes);
        }
    }
}
