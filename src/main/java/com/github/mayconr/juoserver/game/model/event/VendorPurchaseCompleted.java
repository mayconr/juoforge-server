package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.List;

public record VendorPurchaseCompleted(UOPlayer player, List<UOItem> items) implements GameEvent {
}
