package com.github.mayconr.juoserver.infrastructure.serialization;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** Captures a generic target type without depending on a serialization provider. */
public abstract class DataType<T> {
    private final Type type;

    protected DataType() {
        if (!(getClass().getGenericSuperclass() instanceof ParameterizedType parameterized)) {
            throw new IllegalArgumentException("DataType requires a generic type parameter");
        }
        type = parameterized.getActualTypeArguments()[0];
    }

    public final Type type() {
        return type;
    }
}
