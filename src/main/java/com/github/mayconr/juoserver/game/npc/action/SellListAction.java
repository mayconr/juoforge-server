package com.github.mayconr.juoserver.game.npc.action;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;

public record SellListAction(UOPlayer buyer, List<UOItem> itemsToSell) implements NpcAction {
}
