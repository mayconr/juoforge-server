package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class CorpseClothing extends AbstractPacket {

    public static final int CODE = (byte) 0x89;

    private final UOItem corpse;
    private final List<Entry> entries;

    public CorpseClothing(UOItem corpse, List<Entry> entries) {
        super(CODE, computeLength(entries));
        this.corpse = corpse;
        this.entries = List.copyOf(entries);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeInt(corpse.getSerialId());

        for (Entry entry : entries) {
            buf.writeByte((entry.layer().getCode() + 1) & 0xFF);
            buf.writeInt(entry.item().getSerialId());
        }

        buf.writeByte(0x00);
    }

    private static int computeLength(List<Entry> entries) {
        // 1 byte command
        // 2 bytes length
        // 4 bytes corpse serial
        // 5 bytes per entry (1 layer + 4 item serial)
        // 1 byte terminator
        return 1 + 2 + 4 + (entries.size() * 5) + 1;
    }

    public record Entry(Layer layer, UOItem item) {
        public Entry {
            if (layer == null) {
                throw new IllegalArgumentException("layer cannot be null");
            }
        }
    }
}
