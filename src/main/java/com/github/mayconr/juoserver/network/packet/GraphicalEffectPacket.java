package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.EffectType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public class GraphicalEffectPacket extends AbstractPacket {

    public static final int CODE = (byte) 0x70;

    private final EffectType effectType;

    private final int sourceSerial;
    private final int targetSerial;

    private final int graphicId;

    private final Location source;

    private final Location target;

    private final int speed;
    private final int duration;

    private final boolean fixedDirection;
    private final boolean explode;

    public GraphicalEffectPacket(
            EffectType effectType,
            int sourceSerial,
            int targetSerial,
            int graphicId,
            Location source,
            Location target,
            int speed,
            int duration,
            boolean fixedDirection,
            boolean explode) {

        super(CODE, 28);

        this.effectType = effectType;
        this.sourceSerial = sourceSerial;
        this.targetSerial = targetSerial;
        this.graphicId = graphicId;
        this.source = source;
        this.target = target;
        this.speed = speed;
        this.duration = duration;
        this.fixedDirection = fixedDirection;
        this.explode = explode;
    }

    public GraphicalEffectPacket(
            EffectType effectType,
            int graphicId,
            UOObject<?> source,
            UOObject<?> target,
            int speed,
            int duration,
            boolean fixedDirection,
            boolean explode) {
        this(effectType, source.getSerialId(), target.getSerialId(), graphicId, source, target, speed, duration, fixedDirection, explode);
    }

    @Override
    public void writesTo(ByteBuf buf) {

        buf.writeByte(CODE);

        buf.writeByte(effectType.getCode());

        buf.writeInt(sourceSerial);
        buf.writeInt(targetSerial);

        buf.writeShort(graphicId);

        buf.writeShort(source.getX());
        buf.writeShort(source.getY());
        buf.writeByte(source.getZ() + 30);

        buf.writeShort(target.getX());
        buf.writeShort(target.getY());
        buf.writeByte(target.getZ() + 30);

        buf.writeByte(speed);
        buf.writeByte(duration);
        buf.writeShort(0); //unknown
        buf.writeByte(fixedDirection ? 1 : 0);
        buf.writeByte(explode ? 1 : 0);
    }
}
