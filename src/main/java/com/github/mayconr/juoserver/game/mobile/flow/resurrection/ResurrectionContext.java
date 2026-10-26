package com.github.mayconr.juoserver.game.mobile.flow.resurrection;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResurrectionContext extends AbstractContext {
    private final UOMobile mobile;
}

