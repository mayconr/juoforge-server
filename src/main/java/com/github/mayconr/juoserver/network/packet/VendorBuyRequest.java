package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class VendorBuyRequest extends AbstractPacket {
    public static final int CODE = (byte) 0x3B;

    private final int vendorId;
    private final byte flag;
    private final List<BuyItem> items;

    public VendorBuyRequest(ByteBuf buf) {
        super(CODE, -1);

        buf.readByte(); // CODE
        int blockSize = buf.readUnsignedShort();

        this.vendorId = buf.readInt();
        this.flag = buf.readByte();
        this.items = new ArrayList<>();

        if (flag == 0x02) {
            while (buf.readerIndex() < blockSize) {
                byte itemMarker = buf.readByte(); // sempre 0x1A
                if (itemMarker != 0x1A) {
                    break;
                }

                int itemId = buf.readInt();
                int amount = buf.readUnsignedShort();

                items.add(new BuyItem(itemId, amount));
            }
        }
    }

    public record BuyItem(int serialId, int amount) {
    }
}
