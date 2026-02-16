package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.world.WorldModule;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EconomyModule implements WorldModule, EconomyCommands, EconomyQueries {

    private final VendorHandler vendorHandler;
    private final EconomySystem economySystem;

    @Override
    public void update(long tick, double delta) {

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
}
