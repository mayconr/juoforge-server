package com.github.mayconr.juoserver.infrastructure.flow;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class Flow<T extends FlowContext> {

    private final List<FlowStep<T>> steps;

    Flow(List<FlowStep<T>> steps) {
        this.steps = new ArrayList<>(steps);
        this.steps.sort(Comparator.comparing(FlowStep<T>::phase)
                        .thenComparingInt(FlowStep::order));
    }

    public void execute(T context) {
        for (FlowStep<T> step : steps) {
            if (context.isCancelled()) {
                return;
            }

            long start = FlowProfiler.start();

            StepResult result =
                    step.execute(context);

            long duration =
                    FlowProfiler.end(start);

            if (log.isDebugEnabled()) {
                log.debug("{} took {}", step.name(), duration);
            }

            if (result == StepResult.STOP) {
                return;
            }
        }
    }

    public List<FlowStep<T>> steps() {
        return List.copyOf(steps);
    }

}
