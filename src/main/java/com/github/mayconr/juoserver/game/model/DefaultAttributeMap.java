package com.github.mayconr.juoserver.game.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class DefaultAttributeMap implements AttributeMap {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public DefaultAttributeMap(Map<String, Object> initialAttributes) {
        this.attributes.putAll(initialAttributes);
    }

    public DefaultAttributeMap() {
    }

    @Override
    public void set(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
            return;
        }
        attributes.put(key, value);
    }

    @Override
    public Object get(String key) {
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    @Override
    public boolean contains(String key) {
        return attributes.containsKey(key);
    }

    @Override
    public Object remove(String key) {
        return attributes.remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        return (T) attributes.computeIfAbsent(key, k -> supplier.get());
    }

    @Override
    public <T> T compute(String key, BiFunction<String, T, T> remappingFunction) {
        return (T) attributes.compute(key, (k, oldValue) -> remappingFunction.apply(k, (T) oldValue));
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T computeIfPresent(String key, BiFunction<String, T, T> remappingFunction) {
        return (T) attributes.computeIfPresent(
                key,
                (k, oldValue) -> remappingFunction.apply(k, (T) oldValue)
        );
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.copyOf(attributes);
    }
}
