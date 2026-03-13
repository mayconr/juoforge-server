package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TextType {
    NORMAL(0x00),
    BROADCAST(0x01),
    EMOTE(0x02),
    SYSTEM(0x06),
    MESSAGE(0x07),
    WHISPER(0x08),
    YELL(0x09),
    SPELL(0X0A),
    GUILD_CHAT(0x0D),
    ALLIANCE_CHAT(0x0E),
    PROMPT(0x0F);
    private final int code;

    TextType(int code) {
        this.code = code;
    }

    public static TextType byCode(int code) {
        return Arrays.stream(values())
                .filter(value -> value.code == code)
                .findFirst()
                .orElse(TextType.NORMAL);
    }
}
