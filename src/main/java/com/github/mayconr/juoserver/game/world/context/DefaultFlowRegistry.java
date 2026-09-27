package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.*;

public class DefaultFlowRegistry implements FlowRegistry {
    private final Map<Class<?>, Flow<?>> flows = new HashMap<>();

    @Override
    public <T extends AbstractContext> void register(String name, Flow<T> flow, Class<T> contextType) {
        requireNonNull(contextType, "Flow context type is required");
        requireNonNull(flow, "Flow is required");
        if (flows.putIfAbsent(contextType, flow) != null) {
            throw new IllegalArgumentException("Flow already registered for " + contextType.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractContext> Flow<T> get(Class<T> contextType) {
        return (Flow<T>) flows.get(contextType);
    }
}
