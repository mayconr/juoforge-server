package com.github.mayconr.juoserver.infrastructure.flow;

public interface FlowStep<T> {

    String name();

    StepResult execute(T context);

}
