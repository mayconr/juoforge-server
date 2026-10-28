package com.github.mayconr.juoserver.game.ui.flow.tooltip;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class TooltipContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final List<Integer> serials;
}
