package com.github.mayconr.shard.ai.speech;

import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LogNearbySpeechStep extends AbstractFlowStep<SpeechLoggerContext> {
    public LogNearbySpeechStep() {
        super("LogNearbySpeech");
    }

    @Override
    public StepResult execute(SpeechLoggerContext context) {
        MobileSpeech speech;
        while ((speech = context.pollEvent(MobileSpeech.class)) != null) {
            log.info("NPC [{}] (serial={}) heard [{}] (serial={}): {}",
                    context.npc().getName(), context.npc().getSerialId(),
                    speech.mobile().getName(), speech.mobile().getSerialId(), speech.message());
        }
        return StepResult.success();
    }
}
