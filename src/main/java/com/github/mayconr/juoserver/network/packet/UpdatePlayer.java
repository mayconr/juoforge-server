package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class UpdatePlayer extends AbstractPacket {

    public static final int CODE = 0x77;
    private UOPlayer player;

    public UpdatePlayer(UOPlayer player) {
        super(CODE, 17);
        this.player = player;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(player.getSerialId());
        buf.writeShort(player.getModelId());
        buf.writeShort(player.getX());
        buf.writeShort(player.getY());
        buf.writeByte(player.getZ());
        buf.writeByte(player.getDirection().getCode() | (player.isRunning() ? 0x80 : 0));
        buf.writeShort(player.getHue());
        buf.writeByte(player.getStatus().getCode());
        buf.writeByte(player.getNotoriety().getCode());
    }
}
