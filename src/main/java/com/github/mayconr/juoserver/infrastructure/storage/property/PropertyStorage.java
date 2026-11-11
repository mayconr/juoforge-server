package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Database adapter for world properties, supplied by the shard. Names follow
 * {@link WorldKey#validateName(String)}. Supplied nodes are shared and must not be modified.
 */
public interface PropertyStorage {

    /**
     * Atomically upserts and deletes the supplied properties; omitted names are left untouched.
     * A name never appears in both arguments.
     */
    CompletableFuture<Void> saveAll(Map<String, JsonNode> upserts, Set<String> deletes);

    /** Loads every persisted property, indexed by name. */
    CompletableFuture<Map<String, JsonNode>> loadAll();
}
