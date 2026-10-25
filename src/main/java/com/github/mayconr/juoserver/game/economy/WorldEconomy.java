package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import java.util.List;
import java.util.Optional;

/** Public economy operations for regional stock and vendor sessions. */
public interface WorldEconomy {
    /** Finds the nearest stock pool in the region or its ancestors. */
    Optional<StockPool> getStockPool(RegionNode regionNode);

    /** Finds an item in the resolved pool, without falling back to other pools. */
    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);

    /** Runs the flow that creates a priced vendor session and notifies the player. */
    void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items);
}
