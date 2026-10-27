package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record ObjectLabelRequested(UOPlayer player, UOObject<?> object) implements GameEvent { }
