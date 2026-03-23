package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeathScreenType {

    SERVER(0),
    RESURRECT(1),
    GHOST(2);

    private final int code;

}
