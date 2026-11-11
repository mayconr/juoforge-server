package com.github.mayconr.juoserver.infrastructure.storage;

import java.util.concurrent.CompletableFuture;

/**
 * Persistent state coordinated by the world storage during initialization and world saves.
 * Internal to the world storage: public only so implementations in responsibility subpackages can join it.
 */
public interface WorldPersistence {

    /** Loads the persisted state; repeated calls have no effect. */
    void load();

    /** Persists pending changes; completes after the database confirms the write. */
    CompletableFuture<Void> save();
}
