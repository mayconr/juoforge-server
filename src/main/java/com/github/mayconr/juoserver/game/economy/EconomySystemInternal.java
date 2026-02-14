package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

/**
 * Internal contract for accessing regional economic data.
 *
 * <p>Provides read access to stock pools and dynamic price calculation
 * based on region and item template.</p>
 *
 * <p>This interface is intended for internal engine usage and should not
 * be exposed directly to gameplay or external modules.</p>
 */
public interface EconomySystemInternal {

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
}
