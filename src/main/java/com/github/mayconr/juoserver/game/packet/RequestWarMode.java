package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.WarModeType;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class RequestWarMode extends AbstractPacket {

    public static final int CODE = 0x72;
    private final WarModeType type;

    public RequestWarMode(ByteBuf buf) {
        super(CODE, 5);
        buf.readByte(); // code
        this.type = WarModeType.fromCode(buf.readByte());
        buf.readByte(); // unknown
        buf.readByte();
        buf.readByte();
    }

    public RequestWarMode(WarModeType type) {
        super(CODE, 5);
        this.type = type;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(type.getCode());
        buf.writeByte(0);
        buf.writeShort(0);
    }
}
