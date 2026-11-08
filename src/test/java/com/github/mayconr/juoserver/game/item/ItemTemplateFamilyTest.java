package com.github.mayconr.juoserver.game.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.template.CachedItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTemplateFamilyTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void defaultsMissingAndNullFamilyToOther() throws Exception {
        for (String json : List.of("{\"name\":\"default\"}", "{\"name\":\"default\",\"family\":null}")) {
            assertEquals("OTHER", mapper.readValue(json, ItemTemplate.class).family());
        }
        var legacy = new ItemTemplate("legacy", "Legacy", null, 1, true, 0,
                null, 0, List.of(), Map.of(), 0, null, null);
        assertEquals("OTHER", legacy.family());
    }

    @Test
    void preservesStringFamiliesAndRoundTripsJson() throws Exception {
        var item = mapper.readValue("{\"name\":\"ingot\",\"family\":\"MATERIAL\"}", ItemTemplate.class);
        assertEquals("MATERIAL", item.family());
        assertEquals(item, mapper.readValue(mapper.writeValueAsString(item), ItemTemplate.class));
        var custom = mapper.readValue("{\"name\":\"custom\",\"family\":\"SHARD_SPECIFIC\"}", ItemTemplate.class);
        assertEquals("SHARD_SPECIFIC", custom.family());
    }

    @Test
    void indexesMultipleItemsByFamilyAndReturnsEmptyForUnknownFamilies() throws Exception {
        var iron = mapper.readValue("{\"name\":\"iron\",\"family\":\"MATERIAL\"}", ItemTemplate.class);
        var copper = mapper.readValue("{\"name\":\"copper\",\"family\":\"MATERIAL\"}", ItemTemplate.class);
        var other = mapper.readValue("{\"name\":\"other\"}", ItemTemplate.class);
        var items = List.of(iron, copper, other);
        var registry = new CachedItemTemplateRegistry(Map.of("iron", iron, "copper", copper, "other", other));
        assertEquals(2, registry.getByFamily("MATERIAL").size());
        assertTrue(registry.getByFamily("MATERIAL").containsAll(List.of(iron, copper)));
        assertEquals(List.of(other), registry.getByFamily("OTHER"));
        assertTrue(registry.getByFamily("UNKNOWN").isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> registry.getByFamily("MATERIAL").clear());
        var runtimeRegistry = new InMemoryTemplateRegistry<>(items, ItemTemplate::family);
        assertEquals(List.of(iron, copper), runtimeRegistry.get("MATERIAL"));
        assertEquals(List.of(other), runtimeRegistry.get("OTHER"));
        assertTrue(runtimeRegistry.get("UNKNOWN").isEmpty());
    }
}
