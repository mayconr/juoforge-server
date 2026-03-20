package com.github.mayconr.juoserver.infrastructure.template;

import java.util.List;
import java.util.Map;

public interface TemplateLoader<T> {
    default Map<String, T> load() {
        return Map.of();
    };

    default List<T> loadAll() {
        return List.of();
    }
}
