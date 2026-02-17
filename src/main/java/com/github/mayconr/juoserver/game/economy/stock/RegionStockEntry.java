package com.github.mayconr.juoserver.game.economy.stock;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import lombok.Getter;

@Getter
public class RegionStockEntry {
    private final ItemTemplate itemTemplate;
    private final int maxStock;
    private final double elasticity;

    private int currentStock;
    private int producedUnits;
    private int consumedUnits;

    public RegionStockEntry(ItemTemplate itemTemplate,
                            int maxStock,
                            double elasticity,
                            int initialStock) {
        this.itemTemplate = itemTemplate;
        this.maxStock = maxStock;
        this.elasticity = elasticity;
        this.currentStock = initialStock;
    }

    public synchronized void increaseProduction(int amount) {
        if (amount <= 0) return;

        producedUnits += amount;
        currentStock = Math.min(maxStock, currentStock + amount);
    }

    public synchronized void increaseConsumption(int amount) {
        if (amount <= 0) return;

        consumedUnits += amount;
        currentStock = Math.max(0, currentStock - amount);
    }

    public double getScarcityFactor() {
        if (maxStock == 0) return 1.0;
        return 1.0 - ((double) currentStock / maxStock);
    }
}
