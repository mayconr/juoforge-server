package com.github.mayconr.juoserver.infrastructure.flow;

/** Executes registered flows using their exact context class. */
public interface FlowExecutor {
    <T extends AbstractContext> StepResult execute(T context);
}
