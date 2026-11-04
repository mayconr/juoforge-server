package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

import java.nio.charset.StandardCharsets;

@ToString
public final class LanguageExtendedCommand implements ExtendedCommand {

    public static final int SUB_COMMAND = 0x000b;

    private final String language;

    public LanguageExtendedCommand(ByteBuf buf) {
        byte[] lang = new byte[4];
        buf.readBytes(lang);
        int length = 0;
        while (length < lang.length && lang[length] != 0) length++;
        this.language = new String(lang, 0, length, StandardCharsets.US_ASCII);
    }

    public String language() {
        return language;
    }
}
