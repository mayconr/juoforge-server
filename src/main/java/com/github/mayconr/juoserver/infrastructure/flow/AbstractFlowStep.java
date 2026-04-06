package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class AbstractFlowStep<T extends FlowContext> implements FlowStep<T> {

    private final String name;
    private final int order;
    private final FlowPhase phase;

    protected AbstractFlowStep(
            String name,
            int order,
            FlowPhase phase) {

        this.name = name;
        this.order = order;
        this.phase = phase;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public FlowPhase phase() {
        return phase;
    }

}
