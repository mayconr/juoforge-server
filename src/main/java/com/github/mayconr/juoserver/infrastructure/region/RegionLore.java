package com.github.mayconr.juoserver.infrastructure.region;

import java.util.Optional;

/**
 * Narrative information about a region. The object and each of its fields are optional, so a
 * missing field is kept as {@code null} and exposed as {@link Optional#empty()}.
 */
public record RegionLore(String description, String history, String reputation, String secrets) {

    public Optional<String> findDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<String> findHistory() {
        return Optional.ofNullable(history);
    }

    public Optional<String> findReputation() {
        return Optional.ofNullable(reputation);
    }

    public Optional<String> findSecrets() {
        return Optional.ofNullable(secrets);
    }
}
