package com.github.mayconr.juoserver.game.item.flow.split;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import java.util.function.IntConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SplitHeldStackContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final UOItem item;
    private final int amount;
    private final IntConsumer updateHeldAmount;
}
