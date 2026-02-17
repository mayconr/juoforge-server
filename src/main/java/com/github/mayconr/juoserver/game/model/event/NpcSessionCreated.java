package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.ai.session.AISession;

public record NpcSessionCreated(AISession session) implements GameEvent {}
