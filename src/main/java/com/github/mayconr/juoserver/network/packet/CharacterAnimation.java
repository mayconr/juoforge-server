package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.AnimationDirection;
import com.github.mayconr.juoserver.game.model.AnimationRepeat;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class CharacterAnimation extends AbstractPacket {
    private static final int CODE = 0x6E;

    private int serialId;
    private AnimationRepeat repeat;
    private AnimationType type;
    private int frameCount;
    private AnimationDirection direction;

    public CharacterAnimation(
            UOMobile mobile,
            AnimationRepeat repeat,
            AnimationType type,
            int frameCount,
            AnimationDirection direction) {
        super(CODE, 14);
        this.serialId = mobile.getSerialId();
        this.repeat = repeat;
        this.type = type;
        this.frameCount = frameCount;
        this.direction = direction;
    }

    public CharacterAnimation(
            int serialId,
            AnimationRepeat repeat,
            AnimationType type,
            int frameCount,
            AnimationDirection direction) {
        super(CODE, 14);
        this.serialId = serialId;
        this.repeat = repeat;
        this.type = type;
        this.frameCount = frameCount;
        this.direction = direction;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(serialId);
        buf.writeShort(type.getCode());
        buf.writeByte(0);
        buf.writeByte(frameCount);
        buf.writeShort(repeat.getCode());
        buf.writeByte(direction.getCode());
        buf.writeByte(0);
        buf.writeByte(0);
    }
}
