package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.VendorSessionOpened;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class VendorHandler {

    private final EventBus eventBus;
    private final SerialGenerator serialGenerator;
    private final PricingStrategy pricingStrategy;

    public void beginVendorPurchase(UOPlayer player, UOMobile vendor, RegionNode region, List<StockEntry> items) {
        final Map<Integer, VendorSessionItem> vendorSessionItems = new HashMap<>();
        for (StockEntry entry : items) {
            var price = pricingStrategy.calculate(entry, entry.getItemTemplate());
            var serial = serialGenerator.getNextVirtual();
            vendorSessionItems.put(serial, new VendorSessionItem(serial, price, entry));
        }
        var session = new VendorSession(vendorSessionItems);
        player.setVendorSession(session);
        eventBus.publish(new VendorSessionOpened(player, vendor, session));
    }

    public VendorPurchaseResult resolveVendorPurchase(UOPlayer player, VendorBuyRequest vendorBuyRequest, Wallet wallet) {
        final var session = player.getVendorSession();
        final var playerBalance = wallet.getBalance(player);

        if (session == null) {
            log.error("Player {} has no vendor session", player.getName());
            return VendorPurchaseResult.failed();
        }

        final List<PurchasedItem> purchasedItems = new ArrayList<>();
        int totalCost = 0;
        for (VendorBuyRequest.BuyItem buyItem : vendorBuyRequest.getItems()) {
            var item = session.items().get(buyItem.serialId());
            if (item == null) {
                log.error("Item {} not found in vendor session", buyItem.serialId());
                return VendorPurchaseResult.failed();
            }

            totalCost += item.price() * buyItem.amount();

            if (totalCost > playerBalance) {
                return VendorPurchaseResult.failed();
            }
            purchasedItems.add(new PurchasedItem(item.entry().getItemTemplate(), buyItem.amount()));
        }

        if (!wallet.withdraw(player, totalCost)) {
            return VendorPurchaseResult.failed();
        }

        return VendorPurchaseResult.success(purchasedItems);
    }
}
