package com.github.mayconr.juoserver.game.ai.definition.vendor;

import com.github.mayconr.juoserver.game.ai.definition.shared.steps.SpeechFallbackStep;
import com.github.mayconr.juoserver.game.ai.definition.vendor.steps.VendorStep;
import com.github.mayconr.juoserver.game.ai.definition.shared.steps.WanderStep;
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
