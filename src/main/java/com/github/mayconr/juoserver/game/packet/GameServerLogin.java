package com.github.mayconr.juoserver.game.packet;

import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class GameServerLogin extends AbstractPacket {

    public static final int CODE = (byte) 0x91;
    private final int key;
    private final String username;
    private final String password;

    public GameServerLogin(ByteBuf buf) {
        super(CODE, 65);
        buf.readByte();
        this.key = buf.readInt();
        this.username = readStringTrailingZeros(buf, 30, StandardCharsets.UTF_8);
        this.password = readStringTrailingZeros(buf, 30, StandardCharsets.UTF_8);
    }

    public int getKey() {
        return key;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
