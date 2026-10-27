package com.github.mayconr.juoserver.infrastructure.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/** Uses JSON by default; a configured ObjectMapper can supply another format. */
public final class JacksonDataMapper implements DataMapper {
    private final ObjectMapper mapper;

    public JacksonDataMapper() {
        this(new ObjectMapper());
    }

    public JacksonDataMapper(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "ObjectMapper is required")
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String serialize(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to serialize data", exception);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        return read(data, mapper.constructType(type));
    }

    @Override
    public <T> T deserialize(String data, DataType<T> type) {
        return read(data, mapper.constructType(type.type()));
    }

    private <T> T read(String data, JavaType type) {
        try {
            return mapper.readValue(data, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Unable to deserialize data as " + type, exception);
        }
    }

    @Override
    public <T> T convert(Object value, Class<T> type) {
        return mapper.convertValue(value, type);
    }

    @Override
    public <T> T convert(Object value, DataType<T> type) {
        return mapper.convertValue(value, mapper.constructType(type.type()));
    }
}
