package com.github.mayconr.juoserver.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** General-purpose JSON documents. Names are nonblank and at most 100 Unicode characters. */
public interface DocumentStorage {
    /** Atomically upserts the supplied documents; omitted names are left untouched. */
    CompletableFuture<Void> saveAll(Map<String, JsonNode> documents);

    /** Loads every persisted document, indexed by name. */
    CompletableFuture<Map<String, JsonNode>> loadAll();

    static void validateName(String name) {
        if (name == null || name.isBlank() || name.codePointCount(0, name.length()) > 100) {
            throw new IllegalArgumentException("Document name must contain 1 to 100 characters");
        }
    }
}
