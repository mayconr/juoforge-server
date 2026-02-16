package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.List;

public record TooltipRequested(UOPlayer player, List<UOObject> objects) implements GameEvent {
}
