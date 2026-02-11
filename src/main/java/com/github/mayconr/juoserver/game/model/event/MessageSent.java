package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record MessageSent(UOPlayer player, String text, MessageOptions options) implements GameEvent {
}
