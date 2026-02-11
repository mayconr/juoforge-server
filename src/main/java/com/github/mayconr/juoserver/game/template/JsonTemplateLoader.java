package com.github.mayconr.juoserver.game.template;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class JsonTemplateLoader<T extends BaseTemplate> implements TemplateLoader<T> {

    private final ObjectMapper objectMapper;
    private final Path jsonPath;
    private final Class<T> templateType;

    public JsonTemplateLoader(Path jsonPath, Class<T> templateType) {
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                .build();
        this.jsonPath = jsonPath;
        this.templateType = templateType;
    }

    @Override
    public Map<String, T> load() {
        Map<String, T> templates = new HashMap<>();

        try (Stream<Path> files = Files.walk(jsonPath)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> loadFile(path, templates));
            log.info("A total of [{}] {} template were loaded!", templates.size(), templateType.getSimpleName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load templates from " + jsonPath, e);
        }

        return Map.copyOf(templates);
    }

    private void loadFile(Path path, Map<String, T> templates) {
        try {
            var javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, templateType);

            List<T> values = objectMapper.readValue(Files.readAllBytes(path), javaType);

            for (T value : values) {

                var previous = templates.put(value.name(), value);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate npc template name '" + value.name() + "' found in " + path);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse npc template file: " + path, e);
        }
    }
}
