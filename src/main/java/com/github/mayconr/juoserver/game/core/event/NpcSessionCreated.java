package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.session.npc.NpcSession;

public record NpcSessionCreated(NpcSession session) implements GameEvent {}
