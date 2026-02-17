package com.github.mayconr.juoserver.game.item.template;

import com.github.mayconr.juoserver.game.economy.stock.StockType;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.List;
import java.util.Map;

public record ItemTemplate(String name,
                           String displayName,
                           Layer layer,
                           int modelId,
                           boolean movable,
                           int hue,
                           StockType stockType,
                           double basePrice,
                           List<ItemFlag> flags,
                           Map<String, Object> attr)
            implements BaseTemplate {
}
