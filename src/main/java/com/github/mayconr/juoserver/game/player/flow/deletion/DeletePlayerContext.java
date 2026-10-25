package com.github.mayconr.juoserver.game.player.flow.deletion;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractAsyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeletePlayerContext extends AbstractAsyncFlowContext<Void> {
    private final int serialId;
}

