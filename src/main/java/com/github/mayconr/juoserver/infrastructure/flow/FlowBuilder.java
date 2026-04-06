package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FlowBuilder<T extends FlowContext> {

    private final List<FlowStep<T>> steps = new ArrayList<>();
    private final List<FlowHook<T>> hooks = new ArrayList<>();

    public FlowBuilder<T> step(FlowStep<T> step) {
        steps.add(step);
        return this;
    }

    public FlowBuilder<T> hook(
            FlowHook<T> hook) {
        hooks.add(hook);
        return this;
    }

    public FlowBuilder<T> step(FlowStep<T> step, Predicate<T> condition) {
        steps.add(new ConditionalFlowStep<>(step, condition));
        return this;
    }

    public Flow<T> build() {
        applyHooks();
        return new Flow<>(steps);
    }

    private void applyHooks() {
        for (FlowHook<T> hook : hooks) {
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i)
                        .name()
                        .equals(hook.targetStep())) {
                    if (hook.position() == HookPosition.BEFORE) {
                        steps.add(i, hook.step());
                    } else {
                        steps.add(i + 1, hook.step());
                    }
                    break;
                }
            }
        }
    }
}
