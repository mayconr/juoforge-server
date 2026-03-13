package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class LoginConfirm extends AbstractPacket {

    public static final int CODE = (byte) 0x1B;
    private int serialId;
    private int modelId;
    private int x;
    private int y;
    private int z;
    private Direction direction;
    private int mapWidthMinusEight;
    private int mapHeightMinusEight;

    public LoginConfirm(
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            Direction direction,
            int notoriety,
            int mapWidthMinusEight,
            int mapHeightMinusEight) {
        super(CODE, 37);
        this.serialId = serialId;
        this.modelId = modelId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.mapWidthMinusEight = mapWidthMinusEight;
        this.mapHeightMinusEight = mapHeightMinusEight;
    }

    public LoginConfirm(UOMobile mobile, int mapWidthMinusEight, int mapHeightMinusEight) {
        super(CODE, 37);
        this.serialId = mobile.getSerialId();
        this.modelId = mobile.getModelId();
        this.x = mobile.getX();
        this.y = mobile.getY();
        this.z = mobile.getZ();
        this.direction = mobile.getDirection();
        this.mapHeightMinusEight = mapHeightMinusEight;
        this.mapWidthMinusEight = mapWidthMinusEight;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(serialId);
        buf.writeInt(0); // 4 unknown bytes
        buf.writeShort(modelId);
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeByte(0); // unknown byte
        buf.writeByte(z);
        buf.writeByte(direction.getCode());
        buf.writeInt(0); // 4 unknown bytes
        buf.writeInt(0); // 4 unknown bytes
        buf.writeByte(0); // unknown byte
        buf.writeShort(mapHeightMinusEight);
        buf.writeShort(mapWidthMinusEight);
        buf.writeShort(0); // 2 unknown bytes
        buf.writeInt(0); // unknown byte
    }
}
