package com.github.mayconr.juoserver.game.ai.behaviors;

import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;

public class SpeechBehavior implements Behavior {

    private AIContext context;

    private double talkCooldown = 0;
    private static final double COOLDOWN_TIME = 2.0; // 2 secs

    @Override
    public void initialize(AIContext context) {
        this.context = context;
    }

    @Override
    public void onSpeech(UOPlayer player, String text) {
        if (talkCooldown > 0) {
            return; // evita spam
        }

        String normalized = text.toLowerCase();

        if (normalized.contains("hi") || normalized.contains("hello")) {
            context.enqueue(new SpeechAction(new PlainTextMessageContent("Hello, " + player.getName() + "!"), player));
        }
        else {
            context.enqueue(new SpeechAction(new PlainTextMessageContent("I didn't understand you!"), player));
        }

        talkCooldown = COOLDOWN_TIME;
    }

    @Override
    public void onThink(double delta) {
        // reduce cooldown
        if (talkCooldown > 0) {
            talkCooldown -= delta;
        }

        // speak something randomly
        if (talkCooldown <= 0 && Math.random() < 0.001) {
            context.enqueue(new SpeechAction(new PlainTextMessageContent("Beautiful day, isn't it?"), null));
            talkCooldown = COOLDOWN_TIME;
        }
    }
}
