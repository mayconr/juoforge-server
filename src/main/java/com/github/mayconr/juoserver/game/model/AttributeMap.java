package com.github.mayconr.juoserver.game.model;

import java.util.List;
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

    <T> T computeIfPresent(String key, BiFunction<String, T, T> remappingFunction);

    <T> void add(String key, T value);

    <T> List<T> getList(String key);

    <T> boolean removeFromList(String key, T value);

    void clear();

    Map<String, Object> toMap();
}
