package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import java.util.List;
import java.util.Optional;

public interface WorldEconomy {
    void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items);

    Optional<StockPool> getStockPool(RegionNode regionNode);

    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);
}
