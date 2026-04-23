package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

@Getter
public enum ItemLocationType {
    GROUND(0),
    CONTAINER(1),
    EQUIPPED(2),
    ORPHAN(3);

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
