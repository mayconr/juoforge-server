package com.github.mayconr.juoserver.game.npc.action;

import com.github.mayconr.juoserver.game.world.module.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;

public record SellListAction(UOPlayer buyer, List<RegionStockEntry> itemsToSell) implements NpcAction {
}
