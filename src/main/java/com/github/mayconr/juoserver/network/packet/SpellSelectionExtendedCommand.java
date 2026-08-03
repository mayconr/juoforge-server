package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public final class SpellSelectionExtendedCommand implements ExtendedCommand {

    public static final int SUB_COMMAND = 0x001C;
    private final int action;
    private final int spellId;

    public SpellSelectionExtendedCommand(ByteBuf buffer) {
        this.action = buffer.readUnsignedShort();
        this.spellId = buffer.readUnsignedShort();
    }

    public int action() {
        return action;
    }

    public int spellId() {
        return spellId;
    }
}
