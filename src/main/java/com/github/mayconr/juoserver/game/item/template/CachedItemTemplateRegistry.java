package com.github.mayconr.juoserver.game.item.template;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CachedItemTemplateRegistry implements ItemTemplateRegistry {
    private final Map<String, ItemTemplate> byName;
    private final Map<Integer, List<ItemTemplate>> byModelId;
    private final Map<String, ItemTemplate> mountByNpcName;
    private final Map<String, List<ItemTemplate>> byStockType;

    public CachedItemTemplateRegistry(Map<String, ItemTemplate> templates) {
        this.byName = Map.copyOf(templates);
        this.byModelId = templates.values().stream()
                .collect(Collectors.groupingBy(
                    ItemTemplate::modelId,
                    Collectors.collectingAndThen(
                            Collectors.toList(),
                            List::copyOf
                    )
                ));

        mountByNpcName = new HashMap<>();
        templates.values()
            .stream()
            .filter(template->template.attr().containsKey("npcName"))
            .forEach(template->{
                final var key = String.valueOf(template.attr().get("npcName"));
                if (mountByNpcName.containsKey(key)) {
                    log.warn("Duplicate npcName in template {}", template.attr().get("npcName"));
                    return;
                }
                mountByNpcName.put(key, template);
            });

        byStockType = templates.values().stream()
                .filter(template->template.stockType() != null)
                .collect(Collectors.groupingBy(
                        ItemTemplate::stockType,
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

    @Override
    public ItemTemplate getMountByNpcName(String name) {
        return mountByNpcName.get(name);
    }

    @Override
    public List<ItemTemplate> getItemTemplates(String stockType) {
        return byStockType.get(stockType);
    }
}
