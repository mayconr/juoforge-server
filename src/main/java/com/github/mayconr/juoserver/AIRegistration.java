package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.registry.AIContextFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;

import java.util.Objects;
import java.util.function.Function;

/** Configuration parameters for registering an AI context factory and its flow. */
public record AIRegistration<T extends AIFlowContext>(
        String type,
        Class<T> contextType,
        Function<ServerRuntime, AIContextFactory<T>> factory,
        Function<ServerRuntime, Flow<T>> flowFactory) {
    public AIRegistration {
        Objects.requireNonNull(type, "AI type is required");
        Objects.requireNonNull(contextType, "AI context type is required");
        Objects.requireNonNull(factory, "AI context factory is required");
        Objects.requireNonNull(flowFactory, "AI flow factory is required");
        if (type.isBlank()) throw new IllegalArgumentException("AI type must not be blank");
    }
}
