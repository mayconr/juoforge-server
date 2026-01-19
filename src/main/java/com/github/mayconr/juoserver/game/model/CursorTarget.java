package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CursorTarget {
    OBJECT(0),
    LOCATION(1);
    private final int code;

    public static CursorTarget fromCode(int code) {
        for (CursorTarget target : values()) {
            if (target.code == code) {
                return target;
            }
        }
        throw new IllegalArgumentException("Unknown CursorTarget code: " + code);
    }
}
