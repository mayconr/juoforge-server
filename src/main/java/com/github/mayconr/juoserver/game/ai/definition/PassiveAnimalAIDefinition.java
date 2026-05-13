package com.github.mayconr.juoserver.game.ai.definition;

import com.github.mayconr.juoserver.game.ai.definition.steps.WanderStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class PassiveAnimalAIDefinition {

    public static Flow<PassiveAnimalAIContext> build() {
        return FlowFactory.<PassiveAnimalAIContext>builder()
                .step(new WanderStep<>(2.0))
                .build();
    }

}