package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record ItemDroppedInContainer(UOPlayer player, UOContainer container, UOItem item, Location location) implements GameEvent {
}
