package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;

import java.util.List;

public record SellListAction(UONpc seller, UOPlayer buyer, List<StockEntry> itemsToSell) implements NpcAction {
}
