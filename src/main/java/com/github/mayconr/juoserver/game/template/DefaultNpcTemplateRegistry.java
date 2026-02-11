package com.github.mayconr.juoserver.game.template;

import java.util.Map;

public class DefaultNpcTemplateRegistry implements NpcTemplateRegistry {
    private final Map<String, NpcTemplate> byName;

    public DefaultNpcTemplateRegistry(Map<String, NpcTemplate> byName) {
        this.byName = byName;
    }

    @Override
    public NpcTemplate get(String name) {
        var template = byName.get(name);
        if (template == null) {
            throw new IllegalArgumentException("ItemTemplate not found for name: " + name);
        }
        return template;
    }
}
