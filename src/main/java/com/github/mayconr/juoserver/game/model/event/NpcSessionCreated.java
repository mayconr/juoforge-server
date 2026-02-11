package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.npc.NpcSession;

public record NpcSessionCreated(NpcSession session) implements GameEvent {}
