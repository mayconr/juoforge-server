package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;

public record SpeechAction(MessageContent content, UOPlayer speechTo) implements NpcAction {}
