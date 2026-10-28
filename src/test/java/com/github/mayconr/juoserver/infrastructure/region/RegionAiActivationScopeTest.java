package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionAiActivationScopeTest {
    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "omitted"})
    void loadsAiActivationScopeFromRegionJson(String value) throws Exception {
        String field = value.equals("omitted") ? "" : ",\"aiActivationScope\":" + value;
        Files.writeString(directory.resolve("regions.json"), """
                [{"name":"city","displayName":"City","type":"CITY",
                  "area":{"type":"rectangle","x":0,"y":0,"width":10,"height":10}%s}]
                """.formatted(field));

        var regions = new RegionSystemImpl(new JsonTemplateLoader<>(directory, RegionTemplate.class));

        assertEquals(value.equals("true"), regions.getRegion("city").orElseThrow().isAiActivationScope());
    }
}
