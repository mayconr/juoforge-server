package com.github.mayconr.juoserver.game.core.model;

import lombok.Getter;

@Getter
public enum Notoriety {
    INNOCENT(0x1),
    FRIEND(0x2),
    GREY_ANIMAL(0x3),
    CRIMINAL(0x4),
    ENEMY(0x5),
    MURDERER(0x6),
    INVULNERABLE(0x7);

    private final int code;

    Notoriety(int code) {
        this.code = code;
    }

    public static Notoriety fromCode(int code) {
        for (Notoriety status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return Notoriety.INNOCENT;
    }
}
