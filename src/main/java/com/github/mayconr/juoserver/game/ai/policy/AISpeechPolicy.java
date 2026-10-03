package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.vendor.VendorAIContext;
import com.github.mayconr.juoserver.game.model.GameMath;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import java.util.Locale;

@FunctionalInterface
public interface AISpeechPolicy {
    boolean accepts(AIFlowContext context, MobileSpeech speech);

    static AISpeechPolicy nearbyVendor(int radius) {
        if (radius < 0) throw new IllegalArgumentException("radius must be non-negative");
        return (context, speech) -> {
            if (!(context instanceof VendorAIContext)
                    || !(speech.mobile() instanceof UOPlayer player) || !player.isConnected()
                    || speech.message() == null) return false;
            var behavior = context.npc().getBehavior();
            if (behavior == null || behavior.speechTriggers() == null) return false;
            int speechRadius = behavior.speechRadius() == null ? radius : behavior.speechRadius();
            String message = speech.message().strip().toLowerCase(Locale.ROOT);
            return GameMath.isInRange(context.npc(), player, speechRadius)
                    && behavior.speechTriggers().stream()
                    .anyMatch(trigger -> trigger.strip().toLowerCase(Locale.ROOT).equals(message));
        };
    }
}
