package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record StatusGumpRequested(UOPlayer player, UOMobile requestedFor, int weight, int maxWeight) implements GameEvent {
}
