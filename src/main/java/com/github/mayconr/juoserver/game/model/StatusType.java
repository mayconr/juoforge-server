package com.github.mayconr.juoserver.game.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusType {
    GOD_CLIENT(0x00),
    BASIC_STATUS(0x04),
    REQUEST_SKILL(0x05);

    private final int code;

    private static final Map<Integer, StatusType> CODE_MAP = new HashMap<>();

    static {
        for (StatusType type : values()) {
            CODE_MAP.put(type.code, type);
        }
    }

    public static StatusType fromCode(int code) {
        return CODE_MAP.get(code);
    }
}
