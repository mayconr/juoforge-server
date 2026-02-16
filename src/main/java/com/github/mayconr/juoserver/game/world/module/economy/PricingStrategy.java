package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;

public interface PricingStrategy {
    double calculate(RegionStockEntry entry, ItemTemplate template);
}
