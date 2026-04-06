package com.github.mayconr.juoserver.infrastructure.flow;

public interface FlowStep<T extends FlowContext> {

    String name();

    default int order() {
        return 0;
    }

    default FlowPhase phase() {
        return FlowPhase.CORE;
    }

    StepResult execute(T context);

}
