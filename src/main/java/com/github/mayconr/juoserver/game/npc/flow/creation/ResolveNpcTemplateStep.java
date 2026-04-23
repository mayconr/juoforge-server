package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class ResolveNpcTemplateStep extends AbstractFlowStep<NpcCreationContext> {

    private final TemplateRegistry<String, NpcTemplate> templateRegistry;

    public ResolveNpcTemplateStep(int order, TemplateRegistry<String, NpcTemplate> templateRegistry) {
        super("ResolveTemplate", order, FlowPhase.CORE);
        this.templateRegistry = templateRegistry;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        var template = templateRegistry.get(context.getTemplateName())
                .getFirst();
        context.setTemplate(template);
        return StepResult.success();
    }
}
