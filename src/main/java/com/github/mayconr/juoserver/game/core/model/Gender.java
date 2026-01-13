package com.github.mayconr.juoserver.game.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {
    MALE(0),
    FEMALE(1);

    private final int code;

    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return Gender.MALE;
    }
}
