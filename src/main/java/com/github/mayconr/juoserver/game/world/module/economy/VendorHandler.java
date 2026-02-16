package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.BuyGumpSent;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class VendorHandler {

    private final EventBus eventBus;
    private final SerialGenerator serialGenerator;
    private final EconomySystem economySystem;

    public void sendBuyGump(UOPlayer player, UOMobile vendor, RegionNode region, List<RegionStockEntry> items) {
        final List<BuyGumpSent.StockItem> stocks = new ArrayList<>();
        for (RegionStockEntry entry : items) {
            var price = economySystem.getPrice(entry.getItemTemplate(), region.getName());
            stocks.add(new BuyGumpSent.StockItem(serialGenerator.nextItemSerial(), price, entry));
        }
        eventBus.publish(new BuyGumpSent(player, vendor, stocks));
    }
}
