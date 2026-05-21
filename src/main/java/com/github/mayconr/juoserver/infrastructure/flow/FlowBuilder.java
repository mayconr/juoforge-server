package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FlowBuilder<T extends AbstractContext> {

    private final List<FlowStep<T>> steps = new ArrayList<>();
    private final List<FlowHook<T>> hooks = new ArrayList<>();
    private final Map<String, List<FlowHook<T>>> lifecycleHooks = new HashMap<>();

    public FlowBuilder<T> step(FlowStep<T> step) {
        steps.add(step);
        return this;
    }

    public FlowBuilder<T> hook(FlowHook<T> hook) {
        hooks.add(hook);
        return this;
    }

    public FlowBuilder<T> step(FlowStep<T> step, Predicate<T> condition) {
        steps.add(new ConditionalFlowStep<>(step, condition));
        return this;
    }

    public FlowBuilder<T> appendGroup(String name, FlowBuilder<T> builder) {
        this.step(new StepGroupStart<>(name));
        append(builder);
        this.step(new StepGroupEnd<>(name));
        return this;
    }

    public FlowBuilder<T> append(FlowBuilder<T> builder) {
        this.steps.addAll(builder.steps);
        this.hooks.addAll(builder.hooks);
        this.lifecycleHooks.putAll(builder.lifecycleHooks);
        return this;
    }

    public Flow<T> build() {
        applyHooks();
        return new Flow<>(steps, lifecycleHooks);
    }

    private void applyHooks() {
        final var indexedSteps = List.copyOf(steps);
        for (final FlowStep<T> indexedStep : indexedSteps) {
            final var matchingHooks = hooks.stream()
                    .filter(h -> matches(h, indexedStep))
                    .toList();

            // LIFECYCLE
            for (FlowHook<T> hook : matchingHooks) {
                lifecycleHooks
                        .computeIfAbsent(indexedStep.name(), k -> new ArrayList<>())
                        .add(hook);
            }
        }
    }

    private boolean matches(FlowHook<T> hook, FlowStep<T> step) {
        return switch (hook.target()) {
            case AnyHookTarget target -> true;
            case StepNameHookTarget target -> step.name().equals(target.name());
        };
    }
}
