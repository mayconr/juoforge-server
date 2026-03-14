package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Gender {
    MALE(1, List.of(0,2,4,6)),
    FEMALE(2, List.of(1,3,5,7)),;

    private final int code;
    private final List<Integer> packetCodes;

    public static Gender fromPacketCode(int code) {
        for (Gender gender : values()) {
            if (gender.packetCodes.contains(code)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender code: " + code);
    }

    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender code: " + code);
    }
}
