package com.github.mayconr.juoserver.infrastructure.flow;

public interface HookStep<T> {

    void execute(String stepName, StepResult result, T context);

}
