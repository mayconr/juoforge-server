package com.github.mayconr.juoserver.game.loot.flow.generate;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.template.TemplateLoaderStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.loot.PendingLoot;
import com.github.mayconr.juoserver.game.model.ContainerItemTarget;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import java.util.ArrayList;
import java.util.List;

public final class StoreLootTemplatesStep extends AbstractFlowStep<GenerateLootContext> {
    private final TemplateLoaderStep templates;

    public StoreLootTemplatesStep(TemplateRegistry<String, ItemTemplate> names,
                                  TemplateRegistry<Integer, ItemTemplate> models) {
        super("store-loot-templates");
        templates = new TemplateLoaderStep(names, models);
    }

    @Override
    public StepResult execute(GenerateLootContext context) {
        var corpse = context.getRequest().corpse();
        var pending = new ArrayList<ItemRequest>();
        for (var request : context.getItems()) {
            // Resolve the template only. Do not execute item creation or allocate a serial.
            var resolution = new ItemCreationContext(request, ContainerItemTarget.of(corpse));
            var result = templates.execute(resolution);
            if (result.flowFailed()) return result;
            pending.add(new ItemRequest(resolution.getTemplate(), null, null,
                    request.hue(), request.amount(), request.direction()));
        }
        var plan = List.copyOf(pending);
        PendingLoot.store(corpse, plan);
        context.complete(plan);
        return StepResult.success();
    }
}
