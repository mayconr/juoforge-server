package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.economy.stock.StockPool;
import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.economy.flow.purchase.VendorPurchaseContext;
import com.github.mayconr.juoserver.game.economy.flow.begin.BeginVendorPurchaseContext;
import com.github.mayconr.juoserver.game.model.event.VendorPurchaseFailed;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public final class EconomyModuleImpl implements EconomyModule {

    private final EventBus eventBus;
    private final StockHandler stockHandler;
    private final TemplateLoader<RegionStockTemplate> templateLoader;
    private final ItemTemplateRegistry itemTemplateRegistry;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
        var stock = new HashMap<String, StockPool>();
        for (var entry : templateLoader.load().entrySet()) {

            var entries = new HashMap<ItemTemplate, StockEntry>();
            for (var template : entry.getValue().initialStock()) {
                var itemTemplate = itemTemplateRegistry.get(template.itemName());
                entries.put(itemTemplate, new StockEntry(itemTemplate, template.maxStock(), template.elasticity(), template.amount()));
            }

            var region = entry.getValue().region();
            stock.put(region, new StockPool(region, entries));
        }
        stockHandler.initialStock(stock);
    }

    @Override
    public Optional<StockPool> getStockPool(RegionNode regionNode) {
        return stockHandler.getStockPool(regionNode);
    }

    @Override
    public Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode) {
        var pool = stockHandler.getStockPool(regionNode);

        return pool.map(stockPool -> stockPool.getStockEntry(template));
    }

    @Override
    public void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items) {
        flows.execute(new BeginVendorPurchaseContext(player, vendor, region, items));
    }

    @Override
    public void completeVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest) {
        var result = flows.execute(new VendorPurchaseContext(player, vendorBuyRequest, player.getVendorSession()));
        if (result.flowFailed()) {
            eventBus.publish(new VendorPurchaseFailed(player));
        }
    }

}
