package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

import java.util.Map;

public record RegionStockPool(String regionName, Map<ItemTemplate, RegionStockEntry> entries) {
    public RegionStockEntry getStockEntry(ItemTemplate itemTemplate) {
        return entries.get(itemTemplate);
    }
}
