package com.github.mayconr.juoserver.network.packet;

import java.util.Arrays;

import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Target extends AbstractPacket {

    public static final int CODE = (byte) 0x6C;
    private final int cursorId;
    private final CursorTarget target;
    private final CursorType type;
    // Client sent
    private int clickedSerialId;
    private int x;
    private int y;
    private int z;
    private int modelId;

    public Target(int cursorId, CursorTarget target, CursorType type) {
        super(CODE, 19);
        this.cursorId = cursorId;
        this.target = target;
        this.type = type;
    }

    public Target(ByteBuf buf) {
        super(CODE, 19);
        buf.readByte(); // CODE
        this.target = CursorTarget.fromCode(buf.readUnsignedByte());
        this.cursorId = buf.readInt();
        this.type = CursorType.fromCode(buf.readUnsignedByte());
        this.clickedSerialId = buf.readInt();
        this.x = buf.readUnsignedShort();
        this.y = buf.readUnsignedShort();
        buf.readByte(); // unknown
        this.z = buf.readUnsignedByte();
        this.modelId = buf.readUnsignedShort();
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(target.getCode());
        buf.writeInt(cursorId);
        buf.writeByte(type.getCode());
        var noData = new byte[12];
        Arrays.fill(noData, (byte) 0);
        buf.writeBytes(noData);
    }
}
