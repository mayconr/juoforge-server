package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.UOItem;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ObjectInfo extends AbstractPacket {
    public static final int CODE = (byte) 0x1A;

    private final UOItem item;

    public ObjectInfo(UOItem item) {
        super(CODE, computeLength(item));
        this.item = item;
    }

    private static int computeLength(UOItem item) {
        return 1
                + 2
                + 4
                + 2
                + (item.getAmount() > 0 ? 2 : 0)
                + 2
                + 2
                + (item.getDirection() != null ? 1 : 0)
                + 1
                + 2
                + 1;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());

        if (item.getAmount() > 0) {
            buf.writeInt(item.getSerialId() | 0x80000000); // Enable amount
        } else {
            buf.writeInt(item.getSerialId());
        }

        buf.writeShort(item.getModelId());

        if (item.getAmount() > 0) {
            buf.writeShort(item.getAmount());
        }

        if (item.getDirection() != null) {
            buf.writeShort(item.getX() | 0x8000); // sending direction
        } else {
            buf.writeShort(item.getX());
        }

        buf.writeShort(item.getY() | 0x4000 | 0x8000); // 0x8000 enable hue | 0x4000 enable flag

        if (item.getDirection() != null) {
            buf.writeByte(item.getDirection().getCode());
        }
        buf.writeByte(item.getZ());
        buf.writeShort(item.getHue());
        var flag = 0;
        if (!item.isMovable()) {
            flag |= Flag.MOVABLE.getCode();
        }
        if (item.isHidden()) {
            flag |= Flag.HIDDEN.getCode();
        }
        buf.writeByte(flag);
    }

    @RequiredArgsConstructor
    @Getter
    private enum Flag {
        NONE(0x00),
        FAMELE(0x02),
        POISONED(0x04),
        YELLOW_HITS(0x08),
        FACTION_SHIP(0x10),
        MOVABLE(0x20),
        WARMODE(0x40),
        HIDDEN(0x80);
        private final int code;
    }
}
