package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

public final class Weather extends AbstractPacket {
    public static final int CODE = 0x65;

    private final Type type;
    private final int density;
    private final int temperature;

    public Weather(Type type, int density, int temperature) {
        super(CODE, 4);
        this.type = type;
        this.density = density;
        this.temperature = temperature;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(type.code);
        buf.writeByte(density);
        buf.writeByte(temperature);
    }

    public enum Type {
        RAIN(0x00),
        STORM_APPROACH(0x01),
        SNOW(0x02),
        STORM_BREWING(0x03),
        CLEAR(0xFE);

        private final int code;

        Type(int code) {
            this.code = code;
        }
    }
}
