package com.github.mayconr.juoserver.infrastructure.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HookPosition {
    BEFORE,
    AFTER_SUCCESS,
    AFTER_FAILURE
}
