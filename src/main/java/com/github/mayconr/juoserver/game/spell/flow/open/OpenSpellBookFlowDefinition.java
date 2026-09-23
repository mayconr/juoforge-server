package com.github.mayconr.juoserver.game.spell.flow.open;

import com.github.mayconr.juoserver.game.spell.flow.open.notification.NotifySpellBookOpenedStep;
import com.github.mayconr.juoserver.game.spell.flow.open.validation.ValidateSpellBookAccessStep;
import com.github.mayconr.juoserver.game.spell.flow.open.validation.ValidateSpellBookRequestStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class OpenSpellBookFlowDefinition {
    private OpenSpellBookFlowDefinition() {}

    public static Flow<OpenSpellBookContext> build(GameInfra infra) {
        return FlowFactory.<OpenSpellBookContext>builder()
                .step(new ValidateSpellBookRequestStep())
                .step(new ValidateSpellBookAccessStep(infra.storage()))
                .step(new NotifySpellBookOpenedStep(infra.eventBus()))
                .build();
    }
}
