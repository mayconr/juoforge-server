package com.github.mayconr.juoserver.game.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionAttributes {

    private final Map<Key<?>, Object> values = new ConcurrentHashMap<>();

    public <T> void set(Key<T> key, T value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) values.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Key<T> key) {
        return (T) values.remove(key);
    }

    public record Key<T>(String name) {}
}
