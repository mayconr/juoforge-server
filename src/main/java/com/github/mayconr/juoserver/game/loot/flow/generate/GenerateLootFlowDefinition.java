package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.game.loot.LootPolicy;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class GenerateLootFlowDefinition {
    private GenerateLootFlowDefinition() { }

    public static Flow<GenerateLootContext> build(LootPolicy policy, RNG rng,
            TemplateRegistry<String, ItemTemplate> names, TemplateRegistry<Integer, ItemTemplate> models) {
        return FlowFactory.<GenerateLootContext>builder()
                .step(new ValidateLootStep())
                .step(new ReserveLootStep())
                .step(new ResolveLootStep(policy, rng))
                .step(new StoreLootTemplatesStep(names, models))
                .build();
    }
}
