package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Clilocs;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public class TooltipRequest extends AbstractPacket {
    public static final int CODE = (byte) 0xD6;
    @Getter
    private final List<Integer> serialList = new ArrayList<>();
    private UOObject object;

    public TooltipRequest(ByteBuf buf) {
        super(CODE, calculateLength(buf));
        if ((getLength() - 3) % 4 != 0) {
            throw new IllegalStateException(
                    "Bad MegaCliloc message: " + HexFormat.of().formatHex(buf.array()));
        }
        int nQueries = (getLength() - 3) / 4;
        for (int i = 0; i < nQueries; i++) {
            serialList.add(buf.readInt());
        }
    }

    private static int calculateLength(ByteBuf buf) {
        buf.readByte(); // CODE
        return buf.readShort();
    }

    public TooltipRequest(UOObject object) {
        super(CODE, computeLength(object));
        this.object = object;
    }

    private static int computeLength(UOObject object) {
        return 25 + object.getDisplayName().length() * 2;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeShort(0x0001);

        buf.writeInt(object.getSerialId());
        buf.writeShort(0);
        buf.writeInt(Objects.hashCode(object.getSerialId()));

        buf.writeInt(Clilocs.PREFIX_NAME_SUFFIX.getCode());

        final String text = object.getDisplayName();
        buf.writeShort(text.length() * 2);
        if (!text.isEmpty()) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                // Write char as 2-byte little endian (UTF-16LE)
                buf.writeByte(c & 0xFF); // LSB
                buf.writeByte((c >> 8) & 0xFF); // MSB
            }
        }
        buf.writeInt(0); // end byte 25
    }

}
