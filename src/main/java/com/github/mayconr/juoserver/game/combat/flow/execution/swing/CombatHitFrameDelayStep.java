package com.github.mayconr.juoserver.game.combat.flow.execution.swing;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CombatHitFrameDelayStep extends AbstractFlowStep<CombatExecutionContext> {

    private final ScheduledExecutorService scheduler;

    public CombatHitFrameDelayStep() {
        super("CombatHitFrameStep");
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        CompletableFuture<StepResult> future = new CompletableFuture<>();

        long delayMs = context.getHitFrame();

        scheduler.schedule(() -> {
            future.complete(StepResult.success());
        }, delayMs, TimeUnit.MILLISECONDS);

        return StepResult.async(future);
    }
}
