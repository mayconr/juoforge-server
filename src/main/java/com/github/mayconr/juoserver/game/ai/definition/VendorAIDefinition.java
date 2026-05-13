package com.github.mayconr.juoserver.game.ai.definition;

import com.github.mayconr.juoserver.game.ai.definition.steps.SpeechFallbackStep;
import com.github.mayconr.juoserver.game.ai.definition.steps.VendorStep;
import com.github.mayconr.juoserver.game.ai.definition.steps.WanderStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class VendorAIDefinition {
    public static Flow<VendorAIContext> build() {
        return FlowFactory.<VendorAIContext>builder()
                .step(new WanderStep<>(2.0))
                .step(new VendorStep())
                .step(new SpeechFallbackStep<>())
                .build();
    }

}
