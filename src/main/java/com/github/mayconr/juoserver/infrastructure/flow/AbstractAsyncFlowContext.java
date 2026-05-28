package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractAsyncFlowContext<T> extends AbstractContext {

    private final CompletableFuture<T> future = new CompletableFuture<>();

    public void complete(T result) {
        future.complete(result);
    }

    public CompletableFuture<T> result() {
        return future;
    }

    public StepResult fail(Throwable error) {
        future.completeExceptionally(error);
        return StepResult.failure(error.getMessage());
    }
}
