package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UONpc;

public record NpcCreated(UONpc npc) implements GameEvent {
}
