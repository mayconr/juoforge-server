package com.github.mayconr.juoserver.infrastructure.flow;

public abstract class FlowContext {

    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

}
