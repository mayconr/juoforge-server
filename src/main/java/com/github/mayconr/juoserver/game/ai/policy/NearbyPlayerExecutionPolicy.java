package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record NearbyPlayerExecutionPolicy(int radius) implements AIExecutionPolicy {
    public NearbyPlayerExecutionPolicy {
        if (radius < 0) throw new IllegalArgumentException("radius must be non-negative");
    }

    @Override
    public boolean canRun(AIFlowContext context) {
        var behavior = context.npc().getBehavior();
        int activationRadius = behavior != null && behavior.activationRadius() != null
                ? behavior.activationRadius() : radius;
        return !context.world().mobile().getMobilesInRange(context.npc(), activationRadius,
                mobile -> mobile instanceof UOPlayer player && player.isConnected()).isEmpty();
    }
}
