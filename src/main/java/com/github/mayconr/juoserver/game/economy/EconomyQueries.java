package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.Optional;

public interface EconomyQueries {

    /**
     * Returns the stock pool associated with the given region.
     *
     * @param regionName the region identifier
     * @return the region stock pool
     */
    Optional<StockPool> getStockPool(String regionName);

    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);
}
