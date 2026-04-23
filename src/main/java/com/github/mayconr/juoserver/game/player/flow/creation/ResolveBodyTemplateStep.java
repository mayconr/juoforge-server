package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition;
import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.player.template.BodyKey;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public class ResolveBodyTemplateStep extends AbstractFlowStep<PlayerCreationContext> {

    final TemplateRegistry<BodyKey, BodyTemplate> registry;

    public ResolveBodyTemplateStep(TemplateRegistry<BodyKey, BodyTemplate> registry) {
        super("ResolveBodyTemplate");
        this.registry = registry;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        var character = context.getCharacter();

        var template = registry.get(new BodyKey(character.getGender(), character.getRace()))
                .stream().findFirst()
                .orElse(null);

        if (template == null) {
            return StepResult.failure("Body template not found for " + character.getGender() + " " + character.getRace());
        }

        context.setBodyTemplate(template);

        return StepResult.success();
    }
}
