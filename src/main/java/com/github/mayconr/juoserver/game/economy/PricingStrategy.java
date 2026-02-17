package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

public interface PricingStrategy {
    double calculate(RegionStockEntry entry, ItemTemplate template);
}
