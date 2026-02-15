package com.github.mayconr.juoserver.game.world.vendor;

import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.BuyGumpSent;
import com.github.mayconr.juoserver.game.region.RegionNode;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class VendorService {

    private final EventBus eventBus;
    private final SerialGenerator serialGenerator;
    private WorldInternal world;

    public void initialize(WorldInternal  world) {
        this.world = world;
    }

    public void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items) {
        var region = world.resolveRegion(player)
                .map(RegionNode::getName)
                .orElseThrow(() -> new RuntimeException("Invalid player ["+player.getName()+"] region!"));

        List<BuyGumpSent.StockItem> stocks = new ArrayList<>();
        for (RegionStockEntry entry : items) {
            var price = world.getPrice(entry.getItemTemplate(), region);
            stocks.add(new BuyGumpSent.StockItem(serialGenerator.nextItemSerial(), price, entry));
        }
        eventBus.publish(new BuyGumpSent(player, vendor, stocks));
    }
}
