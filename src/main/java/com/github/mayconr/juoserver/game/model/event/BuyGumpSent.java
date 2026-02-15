package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;

public record BuyGumpSent(UOPlayer player, UOMobile vendor, List<StockItem> items) implements GameEvent {

    public record StockItem(int serialId, double price, RegionStockEntry entry) {}

}
