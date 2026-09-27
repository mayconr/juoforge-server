package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.SpellbookType;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * Replaces a spellbook's contents on the client; does not open the book window.
 * The mask uses local positions: bit 0 is spell 1, bit 63 is spell 64.
 * For Magery, use graphic 0x0EFA and scroll offset 1.
 * All 64 spells can be represented by a mask of -1L.
 * ClassicUO reads but does not use the scroll offset; it selects the book type by graphic.
 */
public record NewSpellbookExtendedCommand(int serialId, int graphic, int scrollOffset, long spellMask)
        implements OutboundExtendedCommand {
    public static final int SUB_COMMAND = 0x001B;

    public NewSpellbookExtendedCommand(int serialId, int graphic, SpellbookType type, long spellMask) {
        this(serialId, graphic, Objects.requireNonNull(type, "type").getScrollOffset(), spellMask);
    }

    public NewSpellbookExtendedCommand {
        if (graphic < 0 || graphic > 0xFFFF) {
            throw new IllegalArgumentException("Spellbook graphic must fit an unsigned short");
        }
        if (scrollOffset < 1 || scrollOffset > 0xFFFF) {
            throw new IllegalArgumentException("Spellbook scroll offset must be between 1 and 65535");
        }
    }

    @Override
    public int subCommand() {
        return SUB_COMMAND;
    }

    @Override
    public int payloadLength() {
        return 18;
    }

    @Override
    public void writesTo(ByteBuf buffer) {
        buffer.writeShort(1);
        buffer.writeInt(serialId);
        buffer.writeShort(graphic);
        buffer.writeShort(scrollOffset);
        // ClassicUO reads the mask least-significant byte first, unlike the header fields.
        buffer.writeLongLE(spellMask);
    }
}
