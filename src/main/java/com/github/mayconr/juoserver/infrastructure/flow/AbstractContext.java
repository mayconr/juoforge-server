package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class AbstractContext {

    private final FlowTrace trace = new FlowTrace();

    public FlowTrace trace() {
        return trace;
    }
}
