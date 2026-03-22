package com.github.mayconr.juoserver.infrastructure.template;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class JsonTemplateLoaderNew<T> implements TemplateLoader<T> {

    private final ObjectMapper objectMapper;
    private final Path jsonPath;
    private final Class<T> templateType;

    public JsonTemplateLoaderNew(Path jsonPath, Class<T> templateType) {
        this.objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_YAML_COMMENTS)
                .build();
        this.jsonPath = jsonPath;
        this.templateType = templateType;
    }

    @Override
    public List<T> loadAll() {
        final List<T> templates = new ArrayList<>();

        try (Stream<Path> files = Files.walk(jsonPath)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(path -> loadFile(path, templates));
            log.info("A total of [{}] {} template were loaded!", templates.size(), templateType.getSimpleName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load templates from " + jsonPath, e);
        }

        return List.copyOf(templates);
    }

    private void loadFile(Path path, List<T> templates) {
        try {
            var javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, templateType);

            List<T> values = objectMapper.readValue(Files.readAllBytes(path), javaType);

            templates.addAll(values);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse template file: " + path, e);
        }
    }
}
