package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.core.model.Layer;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class EquipItemRequest extends AbstractPacket {
    public static final int CODE = (byte) 0x13;

    private final int itemSerialId;
    private final Layer layer;
    private final int playerSerialId;

    public EquipItemRequest(ByteBuf buf) {
        super(CODE, 10);
        buf.readByte(); // CODE
        this.itemSerialId = buf.readInt();
        this.layer = Layer.byCode(buf.readByte());
        this.playerSerialId = buf.readInt();
    }
}
