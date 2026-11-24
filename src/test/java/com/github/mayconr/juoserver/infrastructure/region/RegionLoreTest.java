package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionLoreTest {
    @TempDir
    Path directory;

    @Test
    void loadsEveryLoreField() throws Exception {
        var lore = loadLore("""
                ,"lore":{"description":"A port city","history":"Founded by sailors",
                  "reputation":"Safe","secrets":"Smugglers' tunnel"}""").orElseThrow();

        assertEquals(new RegionLore("A port city", "Founded by sailors", "Safe", "Smugglers' tunnel"), lore);
    }

    @Test
    void keepsMissingLoreFieldsEmpty() throws Exception {
        var lore = loadLore(",\"lore\":{\"history\":\"Founded by sailors\"}").orElseThrow();

        assertEquals(Optional.of("Founded by sailors"), lore.findHistory());
        assertTrue(lore.findDescription().isEmpty());
        assertTrue(lore.findReputation().isEmpty());
        assertTrue(lore.findSecrets().isEmpty());
    }

    @Test
    void leavesLoreEmptyWhenOmitted() throws Exception {
        assertTrue(loadLore("").isEmpty());
    }

    private Optional<RegionLore> loadLore(String loreField) throws Exception {
        Files.writeString(directory.resolve("regions.json"), """
                [{"name":"city","displayName":"City","type":"CITY",
                  "area":{"type":"rectangle","x":0,"y":0,"width":10,"height":10}%s}]
                """.formatted(loreField));

        var regions = new RegionSystemImpl(new JsonTemplateLoader<>(directory, RegionTemplate.class));

        return regions.getRegion("city").orElseThrow().getLore();
    }
}
