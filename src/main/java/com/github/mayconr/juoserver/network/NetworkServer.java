package com.github.mayconr.juoserver.network;

import java.util.concurrent.CompletionStage;

/** Lifecycle boundary shared by direct TCP and gateway transports. */
public interface NetworkServer extends AutoCloseable {
    CompletionStage<Void> start();

    @Override
    void close();
}
