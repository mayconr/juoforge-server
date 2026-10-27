package com.github.mayconr.juoserver.game.loot.flow.materialize;

import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.loot.PendingLoot;
import com.github.mayconr.juoserver.infrastructure.serialization.DataMapper;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class MaterializeLootFlowDefinition {
    private MaterializeLootFlowDefinition() { }

    public static Flow<MaterializeLootContext> build(WorldItem items, DataMapper dataMapper) {
        return FlowFactory.<MaterializeLootContext>builder()
                .step(new CreateLootItemsStep(items, new PendingLoot(dataMapper))).build();
    }
}
