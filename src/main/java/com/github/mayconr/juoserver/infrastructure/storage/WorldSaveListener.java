package com.github.mayconr.juoserver.infrastructure.storage;

import java.util.concurrent.CompletableFuture;

/** Persists additional shard state as part of a world save. */
@FunctionalInterface
public interface WorldSaveListener {
    /** Called on the saveWorld caller's thread; return a future covering all asynchronous writes. */
    CompletableFuture<Void> save();
}
