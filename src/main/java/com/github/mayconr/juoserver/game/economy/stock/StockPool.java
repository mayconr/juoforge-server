package com.github.mayconr.juoserver.game.economy.stock;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

import java.util.Map;

public record StockPool(String regionName, Map<ItemTemplate, StockEntry> entries) {
    public StockEntry getStockEntry(ItemTemplate itemTemplate) {
        return entries.get(itemTemplate);
    }
}
