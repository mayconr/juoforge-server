package com.github.mayconr.juoserver.game.template;

import java.util.Map;

public interface TemplateLoader<T> {
    Map<String, T> load();
}
