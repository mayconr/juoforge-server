package com.github.mayconr.juoserver.game.world.module.ai.action;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.module.economy.RegionStockEntry;

import java.util.List;

public record SellListAction(UOPlayer buyer, List<RegionStockEntry> itemsToSell) implements NpcAction {
}
