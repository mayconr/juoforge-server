package com.github.mayconr.juoserver.infrastructure.flow;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Flow<T extends AbstractContext> {

    private final List<FlowStep<T>> steps;

    Flow(List<FlowStep<T>> steps) {
        this.steps = List.copyOf(steps);
    }

    public StepResult execute(T context) {
        return executeStep(context, 0);
    }

    private StepResult executeStep(T context, int index) {

        if (index >= steps.size()) {
            return StepResult.success();
        }

        FlowStep<T> step = steps.get(index);

        long start = System.currentTimeMillis();

        try {

            StepResult result = resolve(step.execute(context));

            log(context, step, start, result);

            if (result.shouldContinue()) {
                return executeStep(context, index + 1);
            }

            return result;

        } catch (Exception e) {

            long elapsed = elapsed(start);

            log.error(step.name(), elapsed, e);

            context.trace().logStep(
                    step.name(),
                    elapsed,
                    "ERROR: " + e.getMessage()
            );

            return StepResult.failure(
                    "FLOW_EXCEPTION",
                    "Step '%s' failed: %s"
                            .formatted(step.name(), e.getMessage())
            );
        }
    }

    private StepResult resolve(StepResult result) {

        return switch (result.status()) {

            case SUCCESS,
                 FAILURE,
                 SKIP,
                 STOP ->
                    result;

            case ASYNC ->
                    resolve(result.next().join());
        };
    }

    private void log(
            T context,
            FlowStep<T> step,
            long start,
            StepResult result
    ) {

        String status = switch (result.status()) {

            case SUCCESS ->
                    "OK";

            case FAILURE ->
                    "FAIL[%s]: %s"
                            .formatted(
                                    result.code(),
                                    result.reason()
                            );

            case SKIP ->
                    result.reason() == null
                            ? "SKIP"
                            : "SKIP[%s]: %s"
                            .formatted(
                                    result.code(),
                                    result.reason()
                            );

            case STOP ->
                    result.reason() == null
                            ? "STOP"
                            : "STOP[%s]: %s"
                            .formatted(
                                    result.code(),
                                    result.reason()
                            );

            case ASYNC ->
                    "ASYNC";
        };

        context.trace().logStep(
                step.name(),
                elapsed(start),
                status
        );
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    public List<FlowStep<T>> steps() {
        return steps;
    }
}
