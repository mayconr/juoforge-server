package com.github.mayconr.juoserver.game.session.npc.action;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.List;

public record BuyListAction(UOMobile buyer, List<UOItem> items) implements NpcAction {
}
