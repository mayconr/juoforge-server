package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface TargetSelectionPolicy {
    Optional<UOPlayer> select(AggressiveAIContext context, List<UOPlayer> candidates);

    static TargetSelectionPolicy nearestPlayer() {
        return (context, candidates) -> candidates.stream().min(
                Comparator.<UOPlayer>comparingLong(player -> Math.max(
                        Math.abs((long) player.getX() - context.npc().getX()),
                        Math.abs((long) player.getY() - context.npc().getY())))
                        .thenComparingInt(UOPlayer::getSerialId));
    }
}
