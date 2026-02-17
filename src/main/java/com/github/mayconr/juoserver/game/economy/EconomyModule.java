package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.RegionStockEntry;
import com.github.mayconr.juoserver.game.economy.stock.RegionStockPool;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.world.WorldModule;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EconomyModule implements WorldModule, EconomyCommands, EconomyQueries {

    private final VendorHandler vendorHandler;
    private final EconomySystem economySystem;

    @Override
    public void update(double delta) {

    }

    @Override
    public void sendBuyGump(UOPlayer player, UOMobile vendor, RegionNode region, List<RegionStockEntry> items) {
        vendorHandler.sendBuyGump(player, vendor, region, items);
    }

    @Override
    public RegionStockPool getStockPool(String regionName) {
        return economySystem.getStockPool(regionName);
    }

    @Override
    public double getPrice(ItemTemplate template, String regionName) {
        return economySystem.getPrice(template, regionName);
    }

    @Override
    public Optional<RegionStockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode) {
        var pool = economySystem.getStockPool(regionNode.getName());
        return Optional.ofNullable(pool.getStockEntry(template));
    }
}
