package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.List;

public record VendorPurchaseResult(boolean success, List<PurchasedItem> items) implements GameEvent {

    public static VendorPurchaseResult failed() {
        return new VendorPurchaseResult(false, null);
    }

    public static VendorPurchaseResult success(List<PurchasedItem> items) {
        return new VendorPurchaseResult(true, items);
    }
}
