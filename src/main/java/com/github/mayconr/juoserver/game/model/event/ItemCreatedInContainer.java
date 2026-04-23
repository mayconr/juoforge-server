package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record ItemCreatedInContainer(UOContainer container, UOItem item, UOMobile owner) implements GameEvent {
}
