package com.github.mayconr.juoserver.game.model;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface AttributeMap {
    void set(String key, Object value);

    Object get(String key);

    <T> T getOrDefault(String key, T defaultValue);

    boolean contains(String key);

    Object remove(String key);

    <T> T computeIfAbsent(String key, Supplier<T> supplier);

    <T> T compute(String key, BiFunction<String, T, T> remappingFunction);

    void clear();

    <T> T computeIfPresent(String key, BiFunction<String, T, T> remappingFunction);

    Map<String, Object> toMap();
}
