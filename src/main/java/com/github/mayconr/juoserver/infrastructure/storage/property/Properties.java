package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

import java.util.Optional;

/**
 * Typed key-value properties of the world, persisted as JSON during {@link WorldStorage#saveWorld()}.
 * Operations only touch memory; values are converted on write and read, so callers always work
 * with copies. Modifying a value returned by {@link #get(WorldKey)} has no effect until it is put again.
 */
public interface Properties {

    <T> Optional<T> get(WorldKey<T> key);

    /** Replaces the value; only the latest value is written by the next world save. */
    <T> void put(WorldKey<T> key, T value);

    /** Removes the value; the deletion is written by the next world save. */
    void remove(WorldKey<?> key);
}
