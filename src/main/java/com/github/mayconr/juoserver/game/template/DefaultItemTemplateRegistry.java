package com.github.mayconr.juoserver.game.template;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultItemTemplateRegistry implements ItemTemplateRegistry {
    private final Map<String, ItemTemplate> byName;
    private final Map<Integer, List<ItemTemplate>> byModelId;

    public DefaultItemTemplateRegistry(Map<String, ItemTemplate> templates) {
        this.byName = Map.copyOf(templates);
        this.byModelId = templates.values().stream()
                .collect(Collectors.groupingBy(
                    ItemTemplate::modelId,
                    Collectors.collectingAndThen(
                            Collectors.toList(),
                            List::copyOf
                    )
                ));
    }

    @Override
    public ItemTemplate get(String name) {
        var template = byName.get(name);
        if (template == null) {
            throw new IllegalArgumentException("ItemTemplate not found for name: " + name);
        }
        return template;
    }

    @Override
    public List<ItemTemplate> get(int modelId) {
        return byModelId.getOrDefault(modelId, List.of());
    }
}
