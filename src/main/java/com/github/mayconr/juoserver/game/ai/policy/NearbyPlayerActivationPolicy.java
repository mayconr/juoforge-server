package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record NearbyPlayerActivationPolicy(int radius) implements AIActivationPolicy {
    public NearbyPlayerActivationPolicy {
        if (radius < 0) throw new IllegalArgumentException("radius must be non-negative");
    }

    @Override
    public boolean isActive(AIFlowContext context) {
        var behavior = context.npc().getBehavior();
        int activationRadius = behavior != null && behavior.activationRadius() != null
                ? behavior.activationRadius() : radius;
        return !context.world().storage().getMobilesInRange(context.npc(), activationRadius,
                mobile -> mobile instanceof UOPlayer player && player.isConnected()).isEmpty();
    }
}
