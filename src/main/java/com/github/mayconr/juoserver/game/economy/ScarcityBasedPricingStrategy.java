package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

public class ScarcityBasedPricingStrategy implements PricingStrategy {

    @Override
    public double calculate(RegionStockEntry entry, ItemTemplate template) {

        double scarcity = entry.getScarcityFactor();
        double elasticity = entry.getElasticity();

        return template.basePrice() * (1 + scarcity * elasticity);
    }
}
