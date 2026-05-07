package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.VendorPurchaseResult;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;

import java.util.List;
import java.util.Optional;

public interface EconomyModule extends WorldModule {

    void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items);

    VendorPurchaseResult resolveVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);

    /**
     * Returns the stock pool associated with the given region.
     *
     * @param regionNode the region identifier
     * @return the region stock pool
     */
    Optional<StockPool> getStockPool(RegionNode regionNode);

    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);
}
