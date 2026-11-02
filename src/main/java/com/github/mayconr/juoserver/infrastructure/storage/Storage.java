package com.github.mayconr.juoserver.infrastructure.storage;

import java.util.concurrent.CompletableFuture;

/** Persists shard state during a full world save. Initialization belongs to the shard. */
@FunctionalInterface
public interface Storage {
    /** Called on the saveWorld caller's thread; return a future covering all asynchronous writes. */
    CompletableFuture<Void> save();
}
