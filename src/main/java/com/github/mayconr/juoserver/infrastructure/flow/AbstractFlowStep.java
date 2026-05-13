package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class AbstractFlowStep<T extends AbstractContext> implements FlowStep<T> {

    private final String name;

    protected AbstractFlowStep(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

}
