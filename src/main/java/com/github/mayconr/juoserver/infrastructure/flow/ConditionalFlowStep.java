package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.function.Predicate;

class ConditionalFlowStep<T extends AbstractContext> implements FlowStep<T> {

    private final FlowStep<T> delegate;
    private final Predicate<T> condition;

    public ConditionalFlowStep(FlowStep<T> delegate, Predicate<T> condition) {
        this.delegate = delegate;
        this.condition = condition;
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public int order() {
        return delegate.order();
    }

    @Override
    public FlowPhase phase() {
        return delegate.phase();
    }

    @Override
    public StepResult execute(T context) {
        if (!condition.test(context)) {
            return StepResult.success(); // ou CONTINUE, se for alias
        }
        return delegate.execute(context);
    }
}
