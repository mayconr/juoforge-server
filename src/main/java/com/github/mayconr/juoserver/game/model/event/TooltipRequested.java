package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.TooltipSupport;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.List;

public record TooltipRequested(UOPlayer player, List<TooltipSupport> objects) implements GameEvent {
}
