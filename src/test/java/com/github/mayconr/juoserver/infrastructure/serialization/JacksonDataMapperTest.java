package com.github.mayconr.juoserver.infrastructure.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JacksonDataMapperTest {
    record Entry(String name, int amount) { }

    private final DataMapper dataMapper = new JacksonDataMapper();

    @Test
    void roundTripsRecordsAndNestedGenericTypes() {
        var entry = new Entry("gold", 10);
        assertEquals(entry, dataMapper.deserialize(dataMapper.serialize(entry), Entry.class));
        var entries = Map.of("loot", List.of(entry));
        assertEquals(entries, dataMapper.deserialize(dataMapper.serialize(entries),
                new DataType<Map<String, List<Entry>>>() { }));
    }

    @Test
    void convertsPlainDataToTypedObjectsWithoutChangingTheSource() {
        var plain = Map.of("name", "gold", "amount", 10);
        var entry = new Entry("gold", 10);
        assertEquals(entry, dataMapper.convert(plain, Entry.class));
        assertEquals(List.of(entry), dataMapper.convert(List.of(plain), new DataType<List<Entry>>() { }));
        assertEquals(List.of(plain), dataMapper.convert(List.of(entry), new DataType<List<Map<String, Object>>>() { }));
        assertEquals(10, plain.get("amount"));
    }

    @Test
    void configuredMapperCanUseAnotherFormatThroughTheSameInterface() {
        DataMapper yaml = new JacksonDataMapper(new ObjectMapper(new YAMLFactory()));
        var entry = new Entry("gold", 10);
        var encoded = yaml.serialize(entry);
        assertTrue(encoded.contains("name: \"gold\""));
        assertEquals(entry, yaml.deserialize(encoded, Entry.class));
    }

    @Test
    void malformedDataRetainsTheOriginalCause() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> dataMapper.deserialize("{", Entry.class));
        assertNotNull(error.getCause());
        assertThrows(IllegalArgumentException.class,
                () -> dataMapper.convert(Map.of("amount", "invalid"), Entry.class));
    }

    @Test
    void gameInfrastructureProvidesADefaultAndRetainsAnInjectedDataMapper() {
        assertInstanceOf(JacksonDataMapper.class, GameInfra.builder().build().dataMapper());
        assertSame(dataMapper, GameInfra.builder().dataMapper(dataMapper).build().dataMapper());
    }
}
