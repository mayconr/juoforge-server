package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CharacterStatus {
    NORMAL(0x00, WarModeType.NORMAL),
    UNKNOWN_1(0x01, WarModeType.NORMAL),
    CAN_ALTER_PAPERBOARD(0x02, WarModeType.NORMAL),
    POISONED(0x04, WarModeType.NORMAL),
    GOLDEN_HEALTH(0x08, WarModeType.NORMAL),
    UNKNOWN_2(0x10, WarModeType.NORMAL),
    UNKNOWN_3(0x20, WarModeType.NORMAL),
    WAR_MODE(0x40, WarModeType.FIGHTING);

    private final int code;
    private final WarModeType warModeType;

    public static CharacterStatus fromCode(int code) {
        for (CharacterStatus d : values()) {
            if (d.code == code) {
                return d;
            }
        }
        return NORMAL;
    }
}
