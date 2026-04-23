package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class SyncFlowContext<T> extends AbstractContext {

    private T result;

    public void complete(T result) {
        this.result = result;
    }

    public T result() {
        return result;
    }
}
