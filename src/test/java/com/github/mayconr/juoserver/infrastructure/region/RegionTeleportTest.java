package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionTeleportTest {
    @TempDir
    Path directory;

    @Test
    void loadsDedicatedTeleportWithZeroHeight() throws IOException {
        var region = loadRegion("""
                ,"teleport":{"kind":"DUNGEON","destination":{"x":1297,"y":1081,"z":0}}
                """);

        assertEquals("DUNGEON", region.getTeleport().kind());
        assertEquals(new PointInTheWorld(1297, 1081, 0), region.getTeleport().destination().toLocation());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            ",\"properties\":{\"teleport.kind\":\"DUNGEON\",\"teleport.destination.x\":1297,"
                    + "\"teleport.destination.y\":1081,\"teleport.destination.z\":0}",
            ",\"teleport\":{\"destination\":{\"x\":1297,\"y\":1081,\"z\":0}}",
            ",\"teleport\":{\"kind\":\"DUNGEON\"}",
            ",\"teleport\":{\"kind\":\"DUNGEON\",\"destination\":{\"x\":1297,\"y\":1081}}"
    })
    void rejectsIncompleteTeleportConfiguration(String fields) {
        var error = assertThrows(IllegalArgumentException.class, () -> loadRegion(fields));
        assertTrue(error.getMessage().contains("'exit' requires teleport."));
    }

    private RegionNode loadRegion(String fields) throws IOException {
        Files.writeString(directory.resolve("regions.json"), """
                [{"name":"exit","displayName":"Exit","type":"TELEPORT",
                  "area":{"type":"rectangle","x":0,"y":0,"width":1,"height":1}%s}]
                """.formatted(fields));

        var regions = new RegionSystemImpl(new JsonTemplateLoader<>(directory, RegionTemplate.class));
        return regions.getRegion("exit").orElseThrow();
    }
}
