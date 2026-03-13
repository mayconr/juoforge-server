package com.github.mayconr.juoserver.game.model;

public enum UseRequestType {
    SKILL(0x24),
    MACRO_SPELL(0x56),
    OPEN_DOOR(0x58),
    ACTION(0xC7);

    private final int code;

    UseRequestType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UseRequestType from(int code) {
        for (var t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return null;
    }
}
