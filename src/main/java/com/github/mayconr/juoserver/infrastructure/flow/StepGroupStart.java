package com.github.mayconr.juoserver.infrastructure.flow;

public record StepGroupStart<T extends AbstractContext>(String name) implements FlowStep<T> {

    @Override
    public String name() {
        return "GroupStart: " + name;
    }

    public StepResult execute(T ctx) {
        ctx.trace().enterGroup(name);
        return StepResult.success();
    }
}
