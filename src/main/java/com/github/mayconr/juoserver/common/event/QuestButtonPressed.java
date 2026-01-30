package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public record QuestButtonPressed(UOPlayer player) implements GameEvent {
}
