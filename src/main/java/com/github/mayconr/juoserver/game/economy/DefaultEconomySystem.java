package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.RegionStockEntry;
import com.github.mayconr.juoserver.game.economy.stock.RegionStockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultEconomySystem implements EconomySystem {

    private final Map<String, RegionStockPool> pools;
    private final PricingStrategy pricingStrategy;

    @Override
    public RegionStockPool getStockPool(String regionName) {
        return pools.get(regionName);
    }

    @Override
    public double getPrice(ItemTemplate template, String regionName) {
        RegionStockPool pool = getStockPool(regionName);
        if (pool == null) {
            throw new IllegalArgumentException("Unknown region: " + regionName);
        }

        RegionStockEntry entry = pool.getStockEntry(template);
        if (entry == null) {
            throw new IllegalArgumentException("Template not configured: " + template);
        }

        return pricingStrategy.calculate(entry, template);
    }

    @Override
    public void recordProduction(String regionName,
                                 ItemTemplate itemTemplate,
                                 int amount) {

        RegionStockPool pool = getStockPool(regionName);
        if (pool == null) return;

        RegionStockEntry entry = pool.getStockEntry(itemTemplate);
        if (entry == null) return;

        entry.increaseProduction(amount);
    }

    @Override
    public void recordConsumption(String regionName,
                                  ItemTemplate itemTemplate,
                                  int amount) {

        RegionStockPool pool = getStockPool(regionName);
        if (pool == null) return;

        RegionStockEntry entry = pool.getStockEntry(itemTemplate);
        if (entry == null) return;

        entry.increaseConsumption(amount);
    }
}

