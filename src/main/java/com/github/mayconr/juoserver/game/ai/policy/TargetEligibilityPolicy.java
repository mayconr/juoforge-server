package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;

@FunctionalInterface
public interface TargetEligibilityPolicy {
    boolean isEligible(AggressiveAIContext context, UOPlayer player);

    /** Default aggressive behavior: any connected, living perceived player may be selected. */
    static TargetEligibilityPolicy connectedAndAlive() {
        return (context, player) -> player.isConnected() && player.isAlive();
    }
}
