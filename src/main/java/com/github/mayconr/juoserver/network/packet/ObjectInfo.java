package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
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
        boolean hasAmount = item.hasFlag(ItemFlag.STACKABLE) && item.getAmount() > 1;
        boolean isCorpse = item.hasFlag(ItemFlag.CORPSE);
        boolean hasCountOrCorpseGraphic = hasAmount || isCorpse;

        boolean hasDirection = item.getDirection() != null;
        boolean hasHue = item.getHue() != 0;
        boolean hasFlags = item.isMovable() || item.isHidden();

        return 1   // code
                + 2 // length
                + 4 // serial
                + 2 // graphic
                + (hasCountOrCorpseGraphic ? 2 : 0)
                + 2 // x
                + 2 // y
                + (hasDirection ? 1 : 0)
                + 1 // z
                + (hasHue ? 2 : 0)
                + (hasFlags ? 1 : 0);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());

        boolean hasAmount = item.hasFlag(ItemFlag.STACKABLE) && item.getAmount() > 1;
        boolean isCorpse = item.hasFlag(ItemFlag.CORPSE);
        boolean hasCountOrCorpseGraphic = hasAmount || isCorpse;

        boolean hasDirection = item.getDirection() != null;
        boolean hasHue = item.getHue() != 0;

        int flag = 0;
        if (item.isMovable()) {
            flag |= Flag.MOVABLE.getCode();
        }
        if (item.isHidden()) {
            flag |= Flag.HIDDEN.getCode();
        }

        boolean hasFlags = flag != 0;

        int serial = item.getSerialId();
        if (hasCountOrCorpseGraphic) {
            serial |= 0x80000000;
        }
        buf.writeInt(serial);

        buf.writeShort(item.getModelId());

        if (hasCountOrCorpseGraphic) {
            if (isCorpse) {
                buf.writeShort(item.getCorpseId() & 0xFFFF);
            } else {
                buf.writeShort(item.getAmount() & 0xFFFF);
            }
        }

        int x = item.getX() & 0x7FFF;
        if (hasDirection) {
            x |= 0x8000;
        }
        buf.writeShort(x);

        int y = item.getY() & 0x3FFF;
        if (hasHue) {
            y |= 0x8000;
        }
        if (hasFlags) {
            y |= 0x4000;
        }
        buf.writeShort(y);

        if (hasDirection) {
            buf.writeByte(item.getDirection().getCode() & 0xFF);
        }
        buf.writeByte(item.getZ());
        if (hasHue) {
            buf.writeShort(item.getHue());
        }
        if (hasFlags) {
            buf.writeByte(flag);
        }
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
