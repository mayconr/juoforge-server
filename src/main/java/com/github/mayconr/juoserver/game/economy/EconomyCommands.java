package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.VendorPurchaseResult;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;

import java.util.List;

public interface EconomyCommands {

    void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items);

    VendorPurchaseResult resolveVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest);
}
