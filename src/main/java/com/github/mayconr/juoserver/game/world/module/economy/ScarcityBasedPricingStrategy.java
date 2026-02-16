package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;

public class ScarcityBasedPricingStrategy implements PricingStrategy {

    @Override
    public double calculate(RegionStockEntry entry, ItemTemplate template) {

        double scarcity = entry.getScarcityFactor();
        double elasticity = entry.getElasticity();

        return template.basePrice() * (1 + scarcity * elasticity);
    }
}
