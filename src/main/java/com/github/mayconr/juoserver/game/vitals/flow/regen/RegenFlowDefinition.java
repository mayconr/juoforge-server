package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class RegenFlowDefinition {
    private RegenFlowDefinition() { }

    public static Flow<RegenContext> build(GamePlaySettings settings, EventBus eventBus) {
        return FlowFactory.<RegenContext>builder()
                .step(new ValidateRegenStep(settings))
                .step(new CalculateRegenStep(settings))
                .step(new ApplyRegenStep())
                .step(new NotifyRegenStep(eventBus))
                .build();
    }
}
