package com.github.mayconr.juoserver.game.item.template;

import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ItemTemplate(String name,
                           String displayName,
                           Layer layer,
                           int modelId,
                           boolean movable,
                           int hue,
                           String stockType,
                           int basePrice,
                           List<ItemFlag> flags,
                           Map<String, Object> attr)

            implements BaseTemplate {
    public ItemTemplate {
        attr = attr == null ? Map.of() : Map.copyOf(attr);
        flags = flags == null ? Collections.emptyList() : flags;
    }
}
