package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.game.loot.LootRequest;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@RequiredArgsConstructor
public final class GenerateLootContext extends AbstractSyncFlowContext<List<ItemRequest>> {
    private final LootRequest request;
    @Setter
    private List<ItemRequest> items = List.of();
}
