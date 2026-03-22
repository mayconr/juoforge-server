package com.github.mayconr.juoserver.game.ai.ais;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.ContextKeys;
import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.AI;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;

import java.util.List;

public class DialogueReactiveAI implements AI {
    @Override
    public String getKey() {
        return "DIALOGUE_REACTIVE";
    }

    @Override
    public void onEvent(AIContext ctx, GameEvent event) {
        switch (event) {
            case MobileSpeech speech -> {
                ctx.set(ContextKeys.SPEAKER, speech.mobile());
                ctx.set(ContextKeys.LAST_SPEECH, speech.message());
            }
            default -> System.out.println("default");
        }
    }

    @Override
    public Behavior decide(AIContext ctx, BehaviorProfile profile) {
        final var npc = ctx.npc();
        final var attributes = npc.persistentAttributes();
        final var supports = (List) attributes.getOrDefault("behavior.supports", List.of());

        if (supports.contains(ctx.get(ContextKeys.LAST_SPEECH, String.class))) {
            return profile.service();
        }
        return profile.talk();
    }

}
