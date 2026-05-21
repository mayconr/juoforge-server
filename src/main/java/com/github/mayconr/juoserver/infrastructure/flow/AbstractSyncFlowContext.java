package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class AbstractSyncFlowContext<T> extends AbstractContext {

    private T result;

    public void complete(T result) {
        this.result = result;
    }

    public T result() {
        if (result == null) throw new IllegalStateException("result is null");
        return result;
    }

    public T result(String nullValueMessage) {
        if (result == null) throw new IllegalStateException(nullValueMessage);
        return result;
    }
}
