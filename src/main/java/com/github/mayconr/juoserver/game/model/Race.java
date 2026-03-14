package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Race {
    HUMAN(1, List.of(0,1,2,3)),
    ELF(2, List.of(4,5)),
    GARGOYLE(3, List.of(6,7)),;

    private final int code;
    private final List<Integer> packetCodes;

    public static Race fromPacketCode(int code) {
        for (Race race : values()) {
            if (race.packetCodes.contains(code)) {
                return race;
            }
        }
        throw new IllegalArgumentException("Invalid race: " + code);
    }

    public static Race fromCode(int code) {
        for (Race race : values()) {
            if (race.code == code) {
                return race;
            }
        }
        throw new IllegalArgumentException("Invalid race: " + code);
    }
}
