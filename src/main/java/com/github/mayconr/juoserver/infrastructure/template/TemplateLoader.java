package com.github.mayconr.juoserver.infrastructure.template;

import java.util.Map;

public interface TemplateLoader<T> {
    Map<String, T> load();
}
