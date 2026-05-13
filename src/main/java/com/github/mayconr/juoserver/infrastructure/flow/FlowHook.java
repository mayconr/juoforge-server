package com.github.mayconr.juoserver.infrastructure.flow;

public class FlowHook<T extends AbstractContext> {

    private final String targetStep;

    private final FlowStep<T> step;

    private final HookPosition position;

    public FlowHook(
            String targetStep,
            FlowStep<T> step,
            HookPosition position) {

        this.targetStep = targetStep;
        this.step = step;
        this.position = position;
    }

    public String targetStep() {
        return targetStep;
    }

    public FlowStep<T> step() {
        return step;
    }

    public HookPosition position() {
        return position;
    }
}
