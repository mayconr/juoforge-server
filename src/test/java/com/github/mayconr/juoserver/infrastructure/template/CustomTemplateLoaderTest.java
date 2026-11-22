package com.github.mayconr.juoserver.infrastructure.template;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.github.mayconr.juoserver.DefaultWorldConfigurator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomTemplateLoaderTest {
    @TempDir
    Path directory;

    public record Example(String id, int amount) {}

    @Test
    void registersAlreadyInstantiatedTemplatesAsAnImmutableSnapshot() {
        var configuration = new DefaultWorldConfigurator();
        var source = new ArrayList<>(List.of(new Example("memory", 1)));
        configuration.addCustomTemplate("example", Example.class, Example::id, source);
        source.clear();
        var entries = configuration.templateList().getFirst().templateLoader().loadAll();
        assertEquals(List.of(new Example("memory", 1)), entries);
        assertThrows(UnsupportedOperationException.class, entries::clear);
        assertThrows(IllegalArgumentException.class,
                () -> configuration.addCustomTemplate("example", Example.class, Example::id, source));
    }

    @Test
    void readsJsonListsAndYamlObjectsAndLists() throws Exception {
        Files.writeString(directory.resolve("one.json"), "[{\"id\":\"json\",\"amount\":1}]");
        Files.writeString(directory.resolve("two.yaml"), "id: yaml\namount: 2\n");
        Files.writeString(directory.resolve("three.yml"), "- id: list\n  amount: 3\n");
        var entries = new JsonTemplateLoaderNew<>(directory, Example.class).loadAll();
        assertEquals(3, entries.size());
        assertEquals(6, entries.stream().mapToInt(Example::amount).sum());
    }

    @Test
    void loadsTemplatesFromDirectoryThroughConfigurator() throws Exception {
        Files.writeString(directory.resolve("one.json"), "[{\"id\":\"json\",\"amount\":1}]");
        Files.writeString(directory.resolve("two.yaml"), "id: yaml\namount: 2\n");
        var entries = new DefaultWorldConfigurator().loadTemplates(directory, Example.class);
        assertEquals(2, entries.size());
        assertThrows(UnsupportedOperationException.class, entries::clear);
    }

    @Test
    void loadsTemplatesFromSingleFileThroughConfigurator() throws Exception {
        var file = directory.resolve("one.json");
        Files.writeString(file, "[{\"id\":\"json\",\"amount\":1}]");
        Files.writeString(directory.resolve("ignored.json"), "[{\"id\":\"other\",\"amount\":2}]");
        var entries = new DefaultWorldConfigurator().loadTemplates(file, Example.class);
        assertEquals(List.of(new Example("json", 1)), entries);
    }

    @Test
    void reportsMissingTemplatePathThroughConfigurator() {
        var configuration = new DefaultWorldConfigurator();
        assertThrows(IllegalStateException.class,
                () -> configuration.loadTemplates(directory.resolve("missing"), Example.class));
        assertThrows(NullPointerException.class, () -> configuration.loadTemplates(null, Example.class));
    }

    @Test
    void reportsMalformedYaml() throws Exception {
        Files.writeString(directory.resolve("broken.yaml"), "id: [unfinished");
        assertThrows(IllegalStateException.class,
                () -> new JsonTemplateLoaderNew<>(directory, Example.class).loadAll());
    }
}
