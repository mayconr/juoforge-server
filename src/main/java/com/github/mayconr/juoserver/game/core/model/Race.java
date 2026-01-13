package com.github.mayconr.juoserver.game.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Race {
    HUMAN(0),
    ELF(1),
    GARGOYLE(2);

    private final int code;

    public static Race fromCode(int code) {
        for (Race race : values()) {
            if (race.code == code) {
                return race;
            }
        }
        return Race.HUMAN;
    }
}
