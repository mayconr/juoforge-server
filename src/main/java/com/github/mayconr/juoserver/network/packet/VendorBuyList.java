package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class VendorBuyList extends AbstractPacket {
    public static final int CODE = (byte) 0x74;

    private final UOObject container;
    private final List<UOItem> items;

    public VendorBuyList(UOObject container, List<UOItem> items) {
        super(CODE, calculateLength(items));
        this.container = container;
        this.items = items;
    }

    private static int calculateLength(List<UOItem> items) {
        int length = 8; // cmd + size + containerSerial + count

        for (UOItem item : items) {
            byte[] nameBytes = item.getDisplayName().getBytes(StandardCharsets.US_ASCII);

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

        for (UOItem item : items) {
            byte[] nameBytes = item.getDisplayName().getBytes(StandardCharsets.US_ASCII);

            buf.writeInt(10); // price
            buf.writeByte(nameBytes.length);
            buf.writeBytes(nameBytes);
        }
    }
}
