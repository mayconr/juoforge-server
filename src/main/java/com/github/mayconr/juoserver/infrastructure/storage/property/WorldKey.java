package com.github.mayconr.juoserver.infrastructure.storage.property;

import com.github.mayconr.juoserver.infrastructure.serialization.DataType;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Typed key of a world property. Declare keys once, usually as constants, so every
 * read and write of the same name uses the same value type.
 *
 * @param <T> type of the stored value
 */
public final class WorldKey<T> {
    public static final int MAX_NAME_LENGTH = 100;

    private final String name;
    private final Type type;

    private WorldKey(String name, Type type) {
        this.name = validateName(name);
        this.type = Objects.requireNonNull(type, "Property type is required");
    }

    public static <T> WorldKey<T> of(String name, Class<T> type) {
        return new WorldKey<>(name, type);
    }

    /** Creates a key for generic values, such as {@code new DataType<List<State>>() {}}. */
    public static <T> WorldKey<T> of(String name, DataType<T> type) {
        Objects.requireNonNull(type, "Property type is required");
        return new WorldKey<>(name, type.type());
    }

    /** Names are nonblank and contain at most {@value #MAX_NAME_LENGTH} Unicode characters. */
    public static String validateName(String name) {
        if (name == null || name.isBlank() || name.codePointCount(0, name.length()) > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Property name must contain 1 to " + MAX_NAME_LENGTH + " characters");
        }
        return name;
    }

    public String name() {
        return name;
    }

    public Type type() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WorldKey<?> key && name.equals(key.name) && type.equals(key.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "WorldKey[" + name + "]";
    }
}
