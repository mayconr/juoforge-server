package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemplateLoaderStep extends AbstractFlowStep<ItemCreationContext> {

    private final TemplateRegistry<String, ItemTemplate> itemTemplateByName;
    private final TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId;

    public TemplateLoaderStep(TemplateRegistry<String, ItemTemplate> itemTemplateByName, TemplateRegistry<Integer, ItemTemplate> itemTemplateByModelId) {
        super("template-loader");
        this.itemTemplateByName = itemTemplateByName;
        this.itemTemplateByModelId = itemTemplateByModelId;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        var request = context.getRequest();

        ItemTemplate template = null;
        if (request.modelId() != null) {
            var templates = itemTemplateByModelId.get(request.modelId());
            if (!templates.isEmpty()) {
                template = templates.getFirst();
            }
        }

        if (request.name() != null) {
            template = itemTemplateByName.get(request.name())
                    .getFirst();
        }

        if (request.template() != null) {
            template = request.template();
        }

        if (template == null) {
            return StepResult.failure("Template not found. Request: " + request);
        }

        context.setTemplate(template);

        return StepResult.success();
    }
}
