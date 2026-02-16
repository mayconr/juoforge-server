package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.Container;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record ContainerOpened(UOPlayer player, Container container) implements GameEvent {
}
