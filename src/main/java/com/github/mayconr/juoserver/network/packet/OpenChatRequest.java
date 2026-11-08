package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import lombok.Getter;

@Getter
public final class OpenChatRequest extends AbstractPacket {
    public static final int CODE = (byte) 0xB5;
    public static final int LENGTH = 64;

    private final String name;

    public OpenChatRequest(ByteBuf buf) {
        super(CODE, LENGTH);
        buf.skipBytes(2); // Opcode and reserved byte.
        var text = buf.readCharSequence(60, StandardCharsets.UTF_16BE).toString();
        int terminator = text.indexOf('\0');
        name = terminator < 0 ? text : text.substring(0, terminator);
        buf.skipBytes(2); // Padding.
    }
}
