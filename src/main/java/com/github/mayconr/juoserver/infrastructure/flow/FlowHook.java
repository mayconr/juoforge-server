package com.github.mayconr.juoserver.infrastructure.flow;

public class FlowHook<T extends AbstractContext> {

    private final HookTarget target;

    private final HookStep<T> step;

    private final HookPosition position;

    public FlowHook(HookTarget target, HookStep<T> step, HookPosition position) {
        this.target = target;
        this.step = step;
        this.position = position;
    }

    public HookTarget target() {
        return target;
    }

    public HookStep<T> step() {
        return step;
    }

    public HookPosition position() {
        return position;
    }
}
