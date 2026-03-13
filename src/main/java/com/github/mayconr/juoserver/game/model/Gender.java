package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {
    HUMAN_MALE(2),
    HUMAN_FEMALE(3);

    private final int code;

    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return Gender.HUMAN_MALE;
    }
}
