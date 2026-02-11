package com.github.mayconr.juoserver.game.npc.action;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public record BuyListAction(UOPlayer buyer) implements NpcAction {
}
