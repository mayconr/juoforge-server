package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.concurrent.CompletableFuture;

public record StepResult(ResultStatus status, CompletableFuture<StepResult> next, String reason) {

    public static StepResult success() {
        return new StepResult(ResultStatus.SUCCESS, null, null);
    }

    public static StepResult failure(String reason) {
        return new StepResult(ResultStatus.FAILURE, null, reason);
    }

    public static StepResult async(CompletableFuture<StepResult> future) {
        return new StepResult(ResultStatus.ASYNC, future, null);
    }

    public static StepResult stop() {
        return new StepResult(ResultStatus.STOP, null, null);
    }

    public static StepResult skip() {
        return new StepResult(ResultStatus.SKIP, null, null);
    }
}
