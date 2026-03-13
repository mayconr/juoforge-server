package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum WarModeType {
    NORMAL(0),
    FIGHTING(1);

    private final int code;

    public static WarModeType fromCode(int code) {
        for (WarModeType mode : WarModeType.values()) {
            if (mode.getCode() == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
