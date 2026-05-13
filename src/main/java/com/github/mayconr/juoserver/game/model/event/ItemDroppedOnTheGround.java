package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record ItemDroppedOnTheGround(UOPlayer player, UOItem item, Location location) implements GameEvent {
}
