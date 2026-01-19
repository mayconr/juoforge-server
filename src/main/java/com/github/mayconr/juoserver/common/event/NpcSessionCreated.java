package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.session.npc.NpcSession;

public record NpcSessionCreated(NpcSession session) implements GameEvent {}
