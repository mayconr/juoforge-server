package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.Optional;

public interface EconomyQueries {

    /**
     * Returns the stock pool associated with the given region.
     *
     * @param regionName the region identifier
     * @return the region stock pool
     */
    RegionStockPool getStockPool(String regionName);

    /**
     * Calculates the dynamic price of an item template for a given region.
     *
     * @param template the item template
     * @param regionName the region identifier
     * @return the calculated price
     */
    double getPrice(ItemTemplate template, String regionName);

    Optional<RegionStockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);
}
