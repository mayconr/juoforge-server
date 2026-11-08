package com.github.mayconr.juoserver.support;

import java.lang.reflect.Field;

/** Access to private instance fields for test fixtures. */
public final class TestFields {
    private TestFields() {}

    public static void setField(Object target, String name, Object value) {
        try {
            field(target, name).set(target, value);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Cannot set field " + name, e);
        }
    }

    public static Object getField(Object target, String name) {
        try {
            return field(target, name).get(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Cannot read field " + name, e);
        }
    }

    private static Field field(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Mockito fixtures may inherit the field from the real class.
            }
        }
        throw new AssertionError("Field not found: " + name);
    }
}
