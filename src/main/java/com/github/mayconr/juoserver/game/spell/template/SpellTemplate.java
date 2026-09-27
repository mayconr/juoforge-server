package com.github.mayconr.juoserver.game.spell.template;

import java.util.Map;

/** Shard spell identity with an optional mapping to the client's spell ID. */
public record SpellTemplate(String key, Integer clientSpellId, String name, Map<String, Object> metadata) {
    public SpellTemplate {
        if (key == null || !key.matches("[a-z0-9_]+:[a-z0-9_]+")) {
            throw new IllegalArgumentException("Spell key must use namespace:name: " + key);
        }
        if (clientSpellId != null && (clientSpellId < 0 || clientSpellId > 0xFFFF)) {
            throw new IllegalArgumentException("Client spell ID must fit an unsigned short: " + clientSpellId);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Spell name must not be blank for key " + key);
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
