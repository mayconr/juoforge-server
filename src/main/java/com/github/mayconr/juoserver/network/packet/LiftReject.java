package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/** Refuses a lift or drop request, telling the client to put the held item back. */
@Getter
public class LiftReject extends AbstractPacket {
    public static final int CODE = (byte) 0x27;

    private final LiftRejectReason reason;

    public LiftReject(LiftRejectReason reason) {
        super(CODE, 2);
        this.reason = reason;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(reason.code());
    }
}
