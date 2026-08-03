package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public final class ClientVersionExtendedCommand implements ExtendedCommand {

    public static final int SUB_COMMAND = 0x0032;

    private final String version;

    public ClientVersionExtendedCommand(ByteBuf buf, int length) {
        byte[] data = new byte[length - 5];
        buf.readBytes(data);
        this.version = new String(data, StandardCharsets.US_ASCII)
                .replace("\0", "");
    }

    public String version() {
        return version;
    }
}
