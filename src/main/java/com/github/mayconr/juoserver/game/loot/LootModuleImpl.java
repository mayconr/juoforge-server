package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.loot.flow.generate.GenerateLootContext;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOCorpse;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.loot.flow.materialize.MaterializeLootContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import java.util.List;
import java.util.Objects;

public final class LootModuleImpl implements LootModule {
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public List<ItemRequest> generate(LootRequest request) {
        Objects.requireNonNull(request);
        synchronized (request.corpse()) {
            var context = new GenerateLootContext(request);
            var result = flows.execute(context);
            if (result.flowFailed()) {
                throw ModuleOperationException.fromFailure("generateLoot", result, null);
            }
            return context.result();
        }
    }
    @Override
    public List<UOItem> materialize(UOCorpse corpse) {
        Objects.requireNonNull(corpse);
        synchronized (corpse) {
            var context = new MaterializeLootContext(corpse);
            var result = flows.execute(context);
            if (result.flowFailed()) {
                throw ModuleOperationException.fromFailure("materializeLoot", result, null);
            }
            return context.result();
        }
    }
}
