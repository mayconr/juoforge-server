package com.github.mayconr.juoserver.game.model.event.message;

import java.util.Map;

public record LocalizedMessageContent(
        String key,
        Map<String, Object> params,
        String fallback
) implements MessageContent {

    public LocalizedMessageContent {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key cannot be null or blank");
        }
        params = params == null ? Map.of() : Map.copyOf(params);
    }

    public LocalizedMessageContent withParam(String name, Object value) {
        var copy = new java.util.HashMap<>(params);
        copy.put(name, value);
        return new LocalizedMessageContent(key, copy, fallback);
    }

}
