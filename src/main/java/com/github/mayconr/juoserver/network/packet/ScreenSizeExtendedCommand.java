package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public final class ScreenSizeExtendedCommand implements ExtendedCommand {

    public static final int SUB_COMMAND = 0x0005;

    private final int width;
    private final int height;

    public ScreenSizeExtendedCommand(ByteBuf buf) {
        buf.readShort();//unknown
        this.width = buf.readUnsignedShort();
        this.height = buf.readUnsignedShort();
        buf.readShort();//unknown
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
