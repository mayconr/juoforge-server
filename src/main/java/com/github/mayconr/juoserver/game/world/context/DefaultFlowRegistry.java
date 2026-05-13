package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;

import java.util.HashMap;
import java.util.Map;

public class DefaultFlowRegistry implements FlowRegistry {
    private final Map<Class<?>, Flow<?>> flows = new HashMap<>();

    @Override
    public <T extends AbstractContext> void register(String name, Flow<T> flow, Class<T> contextType) {
        flows.put(contextType, flow);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractContext> Flow<T> get(Class<T> contextType) {
        return (Flow<T>) flows.get(contextType);
    }
}
