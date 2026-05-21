package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;

public interface FlowRegistry {
    <T extends AbstractContext> void register(String name, Flow<T> flow, Class<T> contextType);

    <T extends AbstractContext> Flow<T> get(Class<T> contextType);
}
