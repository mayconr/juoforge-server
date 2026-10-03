package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.combat.CombatAIContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;

@FunctionalInterface
public interface TargetEligibilityPolicy {
    boolean isEligible(CombatAIContext context, UOPlayer player);

    /** Default aggressive behavior: any connected, living perceived player may be selected. */
    static TargetEligibilityPolicy connectedAndAlive() {
        return (context, player) -> player.isConnected() && player.isAlive();
    }
}
