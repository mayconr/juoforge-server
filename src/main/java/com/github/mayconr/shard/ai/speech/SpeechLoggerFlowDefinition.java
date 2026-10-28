package com.github.mayconr.shard.ai.speech;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class SpeechLoggerFlowDefinition {
    private SpeechLoggerFlowDefinition() {}

    public static Flow<SpeechLoggerContext> build() {
        return FlowFactory.<SpeechLoggerContext>builder()
                .step(new LogNearbySpeechStep())
                .build();
    }
}
