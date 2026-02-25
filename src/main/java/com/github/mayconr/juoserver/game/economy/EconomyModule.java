package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.VendorPurchaseResult;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public final class EconomyModule implements WorldModule, EconomyCommands, EconomyQueries {

    private final VendorHandler vendorHandler;
    private final StockHandler stockHandler;
    private final Wallet wallet;
    private final TemplateLoader<RegionStockTemplate> templateLoader;

    public void initialize(Function<String, ItemTemplate> itemTemplateFunction) {
        var stock = new HashMap<String, StockPool>();
        for (var entry : templateLoader.load().entrySet()) {

            var entries = new HashMap<ItemTemplate, StockEntry>();
            for (var template : entry.getValue().initialStock()) {
                var itemTemplate = itemTemplateFunction.apply(template.itemName());
                entries.put(itemTemplate, new StockEntry(itemTemplate, template.maxStock(), template.elasticity(), template.amount()));
            }

            var region = entry.getValue().region();
            stock.put(region, new StockPool(region, entries));
        }
        stockHandler.initialStock(stock);
    }

    @Override
    public void update(double delta) {}

    @Override
    public void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items) {
        vendorHandler.beginVendorPurchase(player, vendor, region, items);
    }

    @Override
    public VendorPurchaseResult resolveVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest) {
        return vendorHandler.resolveVendorPurchase(player, vendorBuyRequest, wallet);
    }

    @Override
    public Optional<StockPool> getStockPool(String regionName) {
        return stockHandler.getStockPool(regionName);
    }

    @Override
    public Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode) {
        var pool = stockHandler.getStockPool(regionNode.getName());
        var region = regionNode;

        while (pool.isEmpty()) {
            region = region.getParent().orElse(null);
            if (region == null) break;
            pool = stockHandler.getStockPool(region.getName());
        }

        if (log.isDebugEnabled()) {
            if (region != null) {
                log.debug("Stock pool for initial region {} found in {}", regionNode.getName(), region.getName());
            } else {
                log.debug("Stock pool not found region {}", regionNode.getName());
            }
        }

        return pool.map(stockPool -> stockPool.getStockEntry(template));
    }
}
