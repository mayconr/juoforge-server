package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

public class ScarcityBasedPricingStrategy implements PricingStrategy {

    @Override
    public int calculate(StockEntry entry, ItemTemplate template) {

        double scarcity = entry.getScarcityFactor();
        double elasticity = entry.getElasticity();

        return (int) (template.basePrice() * (1 + scarcity * elasticity));
    }
}
