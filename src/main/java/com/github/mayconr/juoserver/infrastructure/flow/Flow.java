package com.github.mayconr.juoserver.infrastructure.flow;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class Flow<T extends AbstractContext> {

    private final List<FlowStep<T>> steps;
    private final Map<String, List<FlowHook<T>>> hooks;

    Flow(List<FlowStep<T>> steps, Map<String, List<FlowHook<T>>> hooks) {
        this.steps = List.copyOf(steps);
        this.hooks = hooks;
        System.out.println(hooks);
    }

    public StepResult execute(T context) {
        return executeStep(context, 0);
    }

    private StepResult executeStep(T context, int index) {
        if (index >= steps.size()) {
            return StepResult.success();
        }

        final var step = steps.get(index);

        long start = System.currentTimeMillis();
        try {
            // Before hooks
            executeHook(step, HookPosition.BEFORE, context);

            // Step execution
            final var result = resolve(step.execute(context));

            log(context, step, start, result);

            // After hooks
            switch (result.status()) {
                case SKIP, SUCCESS, STOP -> executeHook(step, result, HookPosition.AFTER_SUCCESS, context);
                case FAILURE -> executeHook(step, result, HookPosition.AFTER_FAILURE, context);
            }

            if (result.shouldContinue()) {
                return executeStep(context, index + 1);
            }

            return result;

        } catch (Exception e) {
            long elapsed = elapsed(start);
            log.error(step.name(), elapsed, e);
            context.trace().logStep(step.name(), elapsed, "ERROR: " + e.getMessage());

            // Failure Hook
            executeHook(step, HookPosition.AFTER_FAILURE, context);
            return StepResult.failure("FLOW_EXCEPTION", "Step '%s' failed: %s".formatted(step.name(), e.getMessage()));
        }
    }

    private StepResult resolve(StepResult result) {
        return switch (result.status()) {
            case SUCCESS,
                 FAILURE,
                 SKIP,
                 STOP -> result;

            case ASYNC -> resolve(result.next().join());
        };
    }

    private void log(T context, FlowStep<T> step, long start, StepResult result) {
        String status = switch (result.status()) {
            case SUCCESS -> "OK";
            case FAILURE -> "FAIL[%s]: %s".formatted(result.code(), result.reason());
            case SKIP -> result.reason() == null ? "SKIP" : "SKIP[%s]: %s".formatted(result.code(), result.reason());
            case STOP -> result.reason() == null ? "STOP" : "STOP[%s]: %s".formatted(result.code(), result.reason());
            case ASYNC -> "ASYNC";
        };

        context.trace().logStep(step.name(), elapsed(start), status);
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    private void executeHook(FlowStep<T> step, HookPosition position, T context) {
        executeHook(step, null, position, context);
    }

    private void executeHook(FlowStep<T> step, StepResult result, HookPosition position, T context) {
        var stepHooks = hooks.get(step.name());
        if (stepHooks == null || stepHooks.isEmpty()) {
            return;
        }
        for (FlowHook<T> hook : stepHooks) {
            if (position.equals(hook.position())) {
                hook.step().execute(step.name(), result, context);
            }
        }
    }
}
