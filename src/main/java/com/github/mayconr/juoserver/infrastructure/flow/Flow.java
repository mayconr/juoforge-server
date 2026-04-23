package com.github.mayconr.juoserver.infrastructure.flow;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Flow<T extends AbstractContext> {

    private final List<FlowStep<T>> steps;

    Flow(List<FlowStep<T>> steps) {
        this.steps = new ArrayList<>(steps);
    }

    public void execute(T context) {
        executeStep(context, 0);
    }

    private void executeStep(T context, int index) {
        if (index >= steps.size()) {
            return;
        }

        FlowStep<T> step = steps.get(index);

        long start = System.currentTimeMillis();

        try {
            StepResult result = step.execute(context);

            switch (result.status()) {

                case SUCCESS -> {
                    context.trace().logStep(step.name(), elapsed(start), "OK");
                    executeStep(context, index + 1);
                }

                case FAILURE -> {
                    context.trace().logStep(step.name(), elapsed(start), "FAIL: " + result.reason());
                }

                case ASYNC -> {
                    result.next().whenComplete((nextResult, throwable) -> {

                        if (throwable != null) {
                            context.trace().logStep(step.name(), elapsed(start), "FAIL");
                            throw new RuntimeException(throwable);
                        }

                        handleAsyncResult(context, step, start, index, nextResult);
                    });
                }
            }

        } catch (Exception e) {
            log.error(step.name(), elapsed(start), e);
            context.trace().logStep(step.name(), elapsed(start), "FAIL");
            throw e;
        }
    }

    private void handleAsyncResult(
            T context,
            FlowStep<T> step,
            long start,
            int index,
            StepResult result
    ) {
        switch (result.status()) {

            case SUCCESS -> {
                context.trace().logStep(step.name(), elapsed(start), "OK");
                executeStep(context, index + 1);
            }

            case FAILURE -> {
                context.trace().logStep(step.name(), elapsed(start), "FAIL: " + result.reason());
            }

            case ASYNC -> {
                // edge case: async dentro de async
                result.next().whenComplete((r, t) -> {
                    if (t != null) {
                        context.trace().logStep(step.name(), elapsed(start), "FAIL");
                        throw new RuntimeException(t);
                    }
                    handleAsyncResult(context, step, start, index, r);
                });
            }
        }
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    public List<FlowStep<T>> steps() {
        return List.copyOf(steps);
    }

}
