package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record SpellBookOpened(UOPlayer player, int bookSerialId, int modelId,
                              SpellbookType type, long spellMask) implements GameEvent {
}
