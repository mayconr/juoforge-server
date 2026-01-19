package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class ClientVersion extends AbstractPacket {

    public static final int CODE = (byte) 0xBD;
    private String clientVersion;

    public ClientVersion() {
        super(CODE, 3);
    }

    public ClientVersion(ByteBuf buf) {
        this();
        buf.readByte(); // CODE
        int length = buf.readShort();
        this.clientVersion = readStringTrailingZeros(buf, length - 3, StandardCharsets.UTF_8);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
    }

    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public String toString() {
        return "ClientVersion{" + "clientVersion='" + clientVersion + '\'' + '}';
    }
}
