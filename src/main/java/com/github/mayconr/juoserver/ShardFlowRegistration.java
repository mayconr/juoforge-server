package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;

import java.util.Objects;
import java.util.function.Function;

/** A typed flow factory evaluated once during bootstrap, before trigger factories. */
public record ShardFlowRegistration<T extends AbstractContext>(
        Class<T> contextType, Function<ServerRuntime, Flow<T>> factory) {
    public ShardFlowRegistration {
        Objects.requireNonNull(contextType, "Flow context type is required");
        Objects.requireNonNull(factory, "Flow factory is required");
    }

}
