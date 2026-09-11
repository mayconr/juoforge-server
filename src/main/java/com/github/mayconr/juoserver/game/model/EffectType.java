package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EffectType {
    MOVING(0x00),
    LIGHTNING(0x01),
    STATIONARY(0x02);

    private final int code;
}
