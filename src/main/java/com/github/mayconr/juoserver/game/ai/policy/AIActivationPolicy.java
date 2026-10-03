package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import java.util.Objects;

/** Decides whether a session may execute. Policies can inspect NPC and world state. */
@FunctionalInterface
public interface AIActivationPolicy {
    boolean isActive(AIFlowContext context);

    default AIActivationPolicy and(AIActivationPolicy other) {
        Objects.requireNonNull(other);
        return context -> isActive(context) && other.isActive(context);
    }

    default AIActivationPolicy or(AIActivationPolicy other) {
        Objects.requireNonNull(other);
        return context -> isActive(context) || other.isActive(context);
    }
}
