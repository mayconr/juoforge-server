package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class MoveRequest extends AbstractPacket {

    public static final int CODE = (byte) 0x02;
    private Direction direction;
    private final boolean running;
    private final int sequence;
    private final int fastWalkPreventKey;

    public MoveRequest(ByteBuf buf) {
        super(CODE, 7);
        buf.readByte(); // CODE
        final var directionWithRunningInfo = buf.readByte();
        this.direction = Direction.values()[directionWithRunningInfo & 0x07];
        this.running = (directionWithRunningInfo & 0xF0) == 0x80;
        this.sequence = buf.readByte() & 0xFF;
        this.fastWalkPreventKey = buf.readInt();
    }
}
