package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.concurrent.CompletableFuture;

public abstract class AsyncFlowContext<T> extends AbstractContext {

    private final CompletableFuture<T> future = new CompletableFuture<>();

    public StepResult complete(T result) {
        future.complete(result);
        return StepResult.success();
    }

    public CompletableFuture<T> result() {
        return future;
    }

    public StepResult fail(Throwable error) {
        future.completeExceptionally(error);
        return StepResult.failure(error.getMessage());
    }
}
