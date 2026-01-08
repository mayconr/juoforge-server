package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.Direction;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class DrawGamePlayer extends AbstractPacket {

    public static final int CODE = (byte) 0x20;
    private int serialId;
    private int modelId;
    private int hue;
    private int x;
    private int y;
    private int z;
    private Direction direction;

    public DrawGamePlayer(
            int serialId, int modelId, int hue, int x, int y, int z, Direction direction) {
        super(CODE, 19);
        this.serialId = serialId;
        this.modelId = modelId;
        this.hue = hue;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public DrawGamePlayer(UOMobile mobile) {
        super(CODE, 19);
        this.serialId = mobile.getSerialId();
        this.modelId = mobile.getModelId();
        this.hue = mobile.getHue();
        this.x = mobile.getX();
        this.y = mobile.getY();
        this.z = mobile.getZ();
        this.direction = mobile.getDirection();
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(serialId);
        buf.writeShort(modelId);
        buf.writeByte(0);
        buf.writeShort(hue);
        buf.writeByte(0); // flags
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeShort(0);
        buf.writeByte(direction.getCode());
        buf.writeByte(z);
    }
}
