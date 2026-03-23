package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.DeathScreenType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class DeathScreen extends AbstractPacket {

    public static final int CODE = (byte) 0x2C;

    private final DeathScreenType type;

    public DeathScreen(DeathScreenType type) {
        super(CODE, 2);
        this.type = type;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(type.getCode());
    }
}
