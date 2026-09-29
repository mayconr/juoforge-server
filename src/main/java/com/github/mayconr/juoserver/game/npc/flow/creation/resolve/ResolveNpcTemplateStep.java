package com.github.mayconr.juoserver.game.npc.flow.creation.resolve;

import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.npc.NpcRequester;
import lombok.extern.slf4j.Slf4j;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

import java.util.Map;

@Slf4j
public class ResolveNpcTemplateStep extends AbstractFlowStep<NpcCreationContext> {

    private final TemplateRegistry<String, NpcTemplate> templateRegistry;
    private final MessageModule messageModule;

    public ResolveNpcTemplateStep(TemplateRegistry<String, NpcTemplate> templateRegistry, MessageModule messageModule) {
        super("ResolveTemplate");
        this.templateRegistry = templateRegistry;
        this.messageModule = messageModule;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        var template = templateRegistry.get(context.getTemplateName());
        if (template.isEmpty()) {
            switch (context.getRequester()) {
                case NpcRequester.Player requester -> messageModule.send(
                        requester.player(), MessageContent.localized(
                                "createnp.template.notfound", Map.of("templateName", context.getTemplateName())));
                case NpcRequester.AsyncProcess requester -> log.warn(
                        "NPC template {} not found for async process {}", context.getTemplateName(), requester.name());
            }
            return StepResult.stop();
        }

        context.setTemplate(template.getFirst());
        return StepResult.success();
    }
}
