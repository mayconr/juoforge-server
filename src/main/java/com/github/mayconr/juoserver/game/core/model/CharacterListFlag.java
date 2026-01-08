package com.github.mayconr.juoserver.game.core.model;

public enum CharacterListFlag {
    SINGLE_CHAR(0x14),
    MONDAINS_LEGACY(0x1A8),
    ENABLE_AOS_COMMON(0x20),
    ENABLE_NPC_POPUP(0x08),
    CHAR_6TH(0x40),
    SAMURAI_NINJA_CLASSES(0x80),
    ELVEN_RACE(0x100),
    LIMIT_CHAR_SLOTS(0x10),
    UNLOCK_NEW_FELUCCA_AREAS(0x8000);

    private final int code;

    CharacterListFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
