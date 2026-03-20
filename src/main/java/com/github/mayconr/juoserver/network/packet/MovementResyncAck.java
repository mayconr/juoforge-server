package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MovementResyncAck extends AbstractPacket {

    public static final int CODE = (byte) 0x22;

    private final int sequence;
    private final Notoriety notoriety;

    public MovementResyncAck(int sequence, Notoriety notoriety) {
        super(CODE, 3);
        this.sequence = sequence;
        this.notoriety = notoriety;
    }

    public MovementResyncAck(ByteBuf buf) {
        super(CODE, 3);
        buf.readByte(); // CODE
        this.sequence = buf.readByte();
        this.notoriety = Notoriety.fromCode(buf.readByte());
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(sequence);
        buf.writeByte(notoriety.getCode());
    }
}
