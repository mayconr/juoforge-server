package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record TargetSent(UOPlayer player, int id, CursorTarget target, CursorType type) implements GameEvent {
}
