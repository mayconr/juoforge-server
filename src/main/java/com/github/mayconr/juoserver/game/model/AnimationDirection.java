package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AnimationDirection {
    FORWARD(0x00),
    BACKWARD(0X01);

    private final int code;
}
