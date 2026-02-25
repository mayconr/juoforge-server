package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class StockHandler {

    private final Map<String, StockPool> pools = new HashMap<>();

    public void initialStock(Map<String, StockPool> pools) {
        this.pools.putAll(pools);
    }

    public Optional<StockPool> getStockPool(String regionName) {
        return Optional.ofNullable(pools.get(regionName));
    }

}

