package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

@Getter
public enum ItemLocationType {
    GROUND(1),
    CONTAINER(2),
    EQUIPPED(3),
    ORPHAN(4);

    private final int code;

    ItemLocationType(int code) {
        this.code = code;
    }

    public static ItemLocationType fromCode(int code) {
        for (ItemLocationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ItemLocationType code: " + code);
    }
}
