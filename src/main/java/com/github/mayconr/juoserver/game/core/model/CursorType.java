package com.github.mayconr.juoserver.game.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CursorType {
    NEUTRAL(0),
    HARMFUL(1),
    HELPFUL(2),
    CANCEL(3);

    private final int code;

    public static CursorType fromCode(int code) {
        for (CursorType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CursorType code: " + code);
    }
}
