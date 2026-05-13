package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record NpcRemoved(UONpc npc) implements GameEvent {
}
