package com.github.mayconr.juoserver.infrastructure.serialization;

/** Standard infrastructure for serializing data and reconstructing typed objects.
 * The implementation determines the representation (for example JSON, YAML or XML).
 */
public interface DataMapper {
    String serialize(Object value);

    <T> T deserialize(String data, Class<T> type);

    <T> T deserialize(String data, DataType<T> type);

    <T> T convert(Object value, Class<T> type);

    <T> T convert(Object value, DataType<T> type);
}
