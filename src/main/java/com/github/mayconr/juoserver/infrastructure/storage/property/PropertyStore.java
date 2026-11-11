package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.github.mayconr.juoserver.infrastructure.storage.CachedWorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.WorldPersistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory world properties with change tracking. Stored nodes are never modified after
 * insertion, so save snapshots share them without copying. Only changed names are persisted.
 * <p>
 * Internal to the world storage: created by {@link CachedWorldStorage}; consumers use {@link Properties}.
 */
public final class PropertyStore implements Properties, WorldPersistence {
    private final PropertyStorage storage;
    private final ObjectMapper json = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final AtomicLong versions = new AtomicLong();
    private volatile boolean loaded;

    /** @param storage database adapter, or {@code null} when properties are not persisted */
    public PropertyStore(PropertyStorage storage) {
        this.storage = storage;
    }

    @Override
    public <T> Optional<T> get(WorldKey<T> key) {
        Objects.requireNonNull(key, "Property key is required");
        ensureLoaded();
        var entry = entries.get(key.name());
        if (entry == null || entry.isDeleted()) {
            return Optional.empty();
        }
        return Optional.of(read(key, entry.value()));
    }

    @Override
    public <T> void put(WorldKey<T> key, T value) {
        Objects.requireNonNull(key, "Property key is required");
        Objects.requireNonNull(value, "Property value is required");
        ensureLoaded();
        entries.put(key.name(), Entry.changed(toTree(value), versions.incrementAndGet()));
    }

    @Override
    public void remove(WorldKey<?> key) {
        Objects.requireNonNull(key, "Property key is required");
        ensureLoaded();
        // Every persisted name is loaded, so names absent from memory need no deletion.
        entries.computeIfPresent(key.name(), (name, entry) -> Entry.changed(null, versions.incrementAndGet()));
    }

    @Override
    public void load() {
        ensureLoaded();
    }

    @Override
    public CompletableFuture<Void> save() {
        var changes = collectChanges();
        if (changes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        if (storage == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Property storage is not configured"));
        }
        return storage.saveAll(Map.copyOf(changes.upserts()), Set.copyOf(changes.deletes()))
                .thenRun(() -> markSaved(changes.versions()));
    }

    private void ensureLoaded() {
        if (!loaded) {
            loadOnce();
        }
    }

    private synchronized void loadOnce() {
        if (loaded) {
            return;
        }
        if (storage != null) {
            storage.loadAll().join().forEach(this::restore);
        }
        loaded = true;
    }

    private void restore(String name, JsonNode value) {
        WorldKey.validateName(name);
        entries.put(name, Entry.persisted(Objects.requireNonNull(value, "Property value is required")));
    }

    private PendingChanges collectChanges() {
        var changes = new PendingChanges(new HashMap<>(), new HashSet<>(), new HashMap<>());
        entries.forEach(changes::add);
        return changes;
    }

    /** Keeps names changed during the write pending for the next save. */
    private void markSaved(Map<String, Long> savedVersions) {
        savedVersions.forEach((name, version) -> entries.computeIfPresent(
                name,
                (key, entry) -> entry.version() != version ? entry : entry.saved()
        ));
    }

    private JsonNode toTree(Object value) {
        if (value instanceof JsonNode node) {
            return node.deepCopy();
        }
        return json.valueToTree(value);
    }

    private <T> T read(WorldKey<T> key, JsonNode value) {
        try {
            T result = json.convertValue(value, json.constructType(key.type()));
            return copyIfTree(result);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException("Invalid value for world property " + key.name(), error);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T copyIfTree(T value) {
        return value instanceof JsonNode node ? (T) node.deepCopy() : value;
    }

    /** A {@code null} value marks a pending deletion. */
    private record Entry(JsonNode value, long version, boolean changed) {

        static Entry persisted(JsonNode value) {
            return new Entry(value, 0, false);
        }

        static Entry changed(JsonNode value, long version) {
            return new Entry(value, version, true);
        }

        boolean isDeleted() {
            return value == null;
        }

        /** Returns the entry after a successful write; saved deletions leave the store. */
        Entry saved() {
            return isDeleted() ? null : new Entry(value, version, false);
        }
    }

    private record PendingChanges(
            Map<String, JsonNode> upserts,
            Set<String> deletes,
            Map<String, Long> versions
    ) {

        void add(String name, Entry entry) {
            if (!entry.changed()) {
                return;
            }
            if (entry.isDeleted()) {
                deletes.add(name);
            } else {
                upserts.put(name, entry.value());
            }
            versions.put(name, entry.version());
        }

        boolean isEmpty() {
            return versions.isEmpty();
        }
    }
}
