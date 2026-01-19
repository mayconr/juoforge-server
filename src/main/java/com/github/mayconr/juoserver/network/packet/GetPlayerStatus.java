package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.StatusType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class GetPlayerStatus extends AbstractPacket {

    public static final int CODE = (byte) 0x34;
    private final int serialId;
    private final StatusType type;

    public GetPlayerStatus(ByteBuf buf) {
        super(CODE, 10);
        buf.readByte(); // CODE
        buf.readInt(); // unknown
        this.type = StatusType.fromCode(buf.readByte());
        this.serialId = buf.readInt();
    }
}
