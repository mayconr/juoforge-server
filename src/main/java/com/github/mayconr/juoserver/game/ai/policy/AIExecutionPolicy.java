package com.github.mayconr.juoserver.game.ai.policy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import java.util.Objects;

/** Decides whether a session may execute. Policies can inspect NPC and world state. */
@FunctionalInterface
public interface AIExecutionPolicy {
    boolean canRun(AIFlowContext context);

    default AIExecutionPolicy and(AIExecutionPolicy other) {
        Objects.requireNonNull(other);
        return context -> canRun(context) && other.canRun(context);
    }

    default AIExecutionPolicy or(AIExecutionPolicy other) {
        Objects.requireNonNull(other);
        return context -> canRun(context) || other.canRun(context);
    }
}
