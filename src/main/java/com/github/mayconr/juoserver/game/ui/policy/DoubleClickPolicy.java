package com.github.mayconr.juoserver.game.ui.policy;

/** Shard extension point that authorizes a double click before any action is performed. */
@FunctionalInterface
public interface DoubleClickPolicy {
    boolean allows(DoubleClickRequest request);

    static DoubleClickPolicy allowAll() {
        return request -> true;
    }
}
