package com.github.mayconr.juoserver.network.packet;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class DeleteCharacter extends AbstractPacket {

    public static final int CODE = (byte) 0x83;
    private final String password;
    private final Integer selectedSlot;
    private final InetAddress clientIp;

    public DeleteCharacter(ByteBuf buf) {
        super(CODE, 39);
        buf.readByte(); // CODE
        this.password = readStringTrailingZeros(buf, 30, StandardCharsets.UTF_8);
        this.selectedSlot = buf.readInt();
        this.clientIp = readInetAddress(buf);
    }

    public String getPassword() {
        return password;
    }

    public InetAddress getClientIp() {
        return clientIp;
    }

    public Integer getSelectedSlot() {
        return selectedSlot;
    }

    @Override
    public String toString() {
        return "DeleteCharacter{" + "selectedSlot=" + selectedSlot + ", clientIp=" + clientIp + '}';
    }
}
