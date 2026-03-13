package com.github.mayconr.juoserver.network.packet;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class LoginCharacter extends AbstractPacket {

    public static final int CODE = (byte) 0x5D;
    private final String characterName;
    private final Integer loginCount;
    private final Integer selectedSlot;
    private final InetAddress clientIp;

    public LoginCharacter(ByteBuf buf) {
        super(CODE, 73);
        buf.readByte(); // code
        buf.readBytes(4); // pattern1
        this.characterName = readStringTrailingZeros(buf, 30, StandardCharsets.UTF_8);
        buf.readBytes(2); // unknown
        buf.readInt(); // flag
        buf.readBytes(4); // unknown
        this.loginCount = buf.readInt();
        buf.readBytes(16); // unknown
        this.selectedSlot = buf.readInt();
        this.clientIp = readInetAddress(buf);
    }

}
