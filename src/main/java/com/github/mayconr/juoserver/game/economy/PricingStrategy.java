package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

public interface PricingStrategy {
    int calculate(StockEntry entry, ItemTemplate template);
}
