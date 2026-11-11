package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.List;

/** Legacy context menu response (0xBF/0x0014, mode 1), using cliloc offsets from 3000000. */
public record ContextMenuExtendedCommand(int serialId, List<Entry> entries) implements OutboundExtendedCommand {
    public static final int SUB_COMMAND = 0x0014;
    private static final int CLILOC_BASE = 3000000;

    public ContextMenuExtendedCommand {
        entries = List.copyOf(entries);
        if (entries.size() > 255) {
            throw new IllegalArgumentException("Context menu supports at most 255 entries");
        }
    }

    @Override
    public int subCommand() {
        return SUB_COMMAND;
    }

    @Override
    public int payloadLength() {
        return 7 + entries.size() * 6;
    }

    @Override
    public void writesTo(ByteBuf buffer) {
        buffer.writeShort(1);
        buffer.writeInt(serialId);
        buffer.writeByte(entries.size());
        for (var entry : entries) {
            buffer.writeShort(entry.index());
            buffer.writeShort(entry.cliloc() - CLILOC_BASE);
            buffer.writeShort(0); // Enabled, without optional color fields.
        }
    }

    public record Entry(int index, int cliloc) {
        public Entry {
            if (index < 0 || index > 0xFFFF || cliloc < CLILOC_BASE || cliloc > CLILOC_BASE + 0xFFFF) {
                throw new IllegalArgumentException("Context menu index or cliloc is outside the legacy range");
            }
        }
    }
}
