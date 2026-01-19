package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CharacterStatus {
    NORMAL(0x00),
    UNKNOWN_1(0x01),
    CAN_ALTER_PAPERBOARD(0x02),
    POISONED(0x04),
    GOLDEN_HEALTH(0x08),
    UNKNOWN_2(0x10),
    UNKNOWN_3(0x20),
    WAR_MODE(0x40);

    private final int code;

    public static CharacterStatus fromCode(int code) {
        for (CharacterStatus d : values()) {
            if (d.code == code) {
                return d;
            }
        }
        return NORMAL;
    }
}
