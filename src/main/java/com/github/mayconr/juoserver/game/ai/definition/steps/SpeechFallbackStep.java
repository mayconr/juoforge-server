package com.github.mayconr.juoserver.game.ai.definition.steps;

import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class SpeechFallbackStep<T extends AIFlowContext> extends AbstractFlowStep<T> {
    public SpeechFallbackStep() {
        super("SpeechFallback");
    }

    @Override
    public StepResult execute(AIFlowContext ctx) {

        // 👇 consome UM evento de speech
        MobileSpeech event = ctx.pollEvent(MobileSpeech.class);

        if (event == null) {
            return StepResult.success();
        }

        String normalized = event.message().toLowerCase().trim();

        switch (normalized) {
            case "hello", "hi", "oi", "ola" -> {
                ctx.enqueueAction(new SpeechAction(ctx.npc(), (UOPlayer) event.mobile(), MessageContent.plain("Hello, traveler.")));
                return StepResult.success();
            }
        }

        return StepResult.success();
    }
}
