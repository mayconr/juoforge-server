package com.github.mayconr.juoserver.game.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AnimationRepeat {
    ONCE(1),
    TWICE(2),
    FOREVER(0);

    private final int code;
}
