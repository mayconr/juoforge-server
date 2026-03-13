package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

@Getter
public enum SkillLock {

    UP(0),
    DOWN(1),
    LOCKED(2);

    private final int code;

    SkillLock(int code) {
        this.code = code;
    }

    public static SkillLock fromCode(int code) {
        return switch (code) {
            case 0 -> UP;
            case 1 -> DOWN;
            case 2 -> LOCKED;
            default -> throw new IllegalArgumentException(
                    "Unknown SkillLock code: " + code
            );
        };
    }
}
