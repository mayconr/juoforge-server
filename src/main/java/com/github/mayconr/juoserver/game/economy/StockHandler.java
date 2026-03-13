package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class StockHandler {

    private final Map<String, StockPool> pools = new HashMap<>();

    public void initialStock(Map<String, StockPool> pools) {
        this.pools.putAll(pools);
    }

    public Optional<StockPool> getStockPool(RegionNode regionNode) {

        RegionNode current = regionNode;

        while (current != null) {
            var pool = pools.get(current.getName());
            if (pool != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Stock pool for region {} resolved in {}", regionNode.getName(), current.getName());
                }
                return Optional.of(pool);
            }

            current = current.getParent().orElse(null);
        }

        if (log.isDebugEnabled()) {
            log.debug("Stock pool not found for region {}", regionNode.getName());
        }

        return Optional.empty();
    }

}

