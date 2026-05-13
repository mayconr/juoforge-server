package com.github.mayconr.juoserver.infrastructure.flow;

public record StepGroupEnd<T extends AbstractContext>(String name) implements FlowStep<T> {

    @Override
    public String name() {
        return "GroupEnd: " + name;
    }

    public StepResult execute(T ctx) {
        ctx.trace().exitGroup(name);
        return StepResult.success();
    }
}
