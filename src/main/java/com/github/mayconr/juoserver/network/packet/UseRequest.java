package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.UseRequestType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class UseRequest extends AbstractPacket {
    public static final int CODE = (byte) 0x12;

    private final UseRequestType type;
    private final String value;

    public UseRequest(ByteBuf buf) {
        super(CODE, -1);

        buf.readByte(); // cmd 0x12
        int blockSize = buf.readUnsignedShort();
        int typeCode = buf.readUnsignedByte();

        this.type = UseRequestType.from(typeCode);

        int payloadSize = blockSize - 4;

        if (payloadSize > 0) {
            this.value = readNullTerminatedAsciiString(buf);
        } else {
            this.value = null;
        }
    }
}
