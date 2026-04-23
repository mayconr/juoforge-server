package com.github.mayconr.juoserver.infrastructure.flow;

public final class FlowFactory {

    private FlowFactory() {}

    public static <T extends AbstractContext> FlowBuilder<T> builder() {
        return new FlowBuilder<>();
    }
}
