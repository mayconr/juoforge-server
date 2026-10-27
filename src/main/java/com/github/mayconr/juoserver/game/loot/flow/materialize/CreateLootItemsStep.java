package com.github.mayconr.juoserver.game.loot.flow.materialize;

import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.loot.PendingLoot;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.ArrayList;
import java.util.List;

public final class CreateLootItemsStep extends AbstractFlowStep<MaterializeLootContext> {
    private final WorldItem items;
    private final PendingLoot pendingLoot;

    public CreateLootItemsStep(WorldItem items, PendingLoot pendingLoot) {
        super("materialize-loot-items");
        this.items = items;
        this.pendingLoot = pendingLoot;
    }

    @Override
    public StepResult execute(MaterializeLootContext context) {
        var corpse = context.getCorpse();
        if (corpse == null) return StepResult.failure("MISSING_LOOT_CORPSE", "Corpse is required");
        synchronized (corpse) {
            var pending = pendingLoot.read(corpse);
            // Keep single-attempt behavior even if item creation fails after partially applying the plan.
            pendingLoot.clear(corpse);
            var created = new ArrayList<UOItem>();
            for (var request : pending) {
                created.add(items.createItem(request,
                        ContainerItemTarget.of(corpse, options -> options.tryStack(false)),
                        options -> options.renderOnCreate(false)));
            }
            context.complete(List.copyOf(created));
        }
        return StepResult.success();
    }
}
