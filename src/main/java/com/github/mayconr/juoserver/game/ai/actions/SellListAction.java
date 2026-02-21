package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;

import java.util.List;

public record SellListAction(UOPlayer buyer, List<StockEntry> itemsToSell) implements NpcAction {
}
