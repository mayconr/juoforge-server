package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

public interface PricingStrategy {
    double calculate(RegionStockEntry entry, ItemTemplate template);
}
