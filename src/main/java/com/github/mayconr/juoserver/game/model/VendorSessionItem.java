package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;

import java.util.Optional;

public record VendorSessionItem(int serialId, int price, StockEntry entry) implements TooltipSupport{
    @Override
    public int getSerialId() {
        return serialId;
    }

    @Override
    public String getTooltipText() {
        return Optional.ofNullable(entry)
                .map(StockEntry::getItemTemplate)
                .map(ItemTemplate::displayName)
                .orElse(null);
    }
}
