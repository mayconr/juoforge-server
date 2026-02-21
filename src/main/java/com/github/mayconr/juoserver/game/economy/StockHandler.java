package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class StockHandler {

    private final Map<String, StockPool> pools = new HashMap<>();

    public void initialStock(Map<String, StockPool> pools) {
        this.pools.putAll(pools);
    }

    public StockPool getStockPool(String regionName) {
        return pools.get(regionName);
    }

    public void recordProduction(String regionName,
                                 ItemTemplate itemTemplate,
                                 int amount) {

        StockPool pool = getStockPool(regionName);
        if (pool == null) return;

        StockEntry entry = pool.getStockEntry(itemTemplate);
        if (entry == null) return;

        entry.increaseProduction(amount);
    }

    public void recordConsumption(String regionName,
                                  ItemTemplate itemTemplate,
                                  int amount) {

        StockPool pool = getStockPool(regionName);
        if (pool == null) return;

        StockEntry entry = pool.getStockEntry(itemTemplate);
        if (entry == null) return;

        entry.increaseConsumption(amount);
    }
}

