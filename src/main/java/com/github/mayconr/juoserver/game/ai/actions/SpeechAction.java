package com.github.mayconr.juoserver.game.ai.actions;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;

public record SpeechAction(
        UONpc speaker,
        UOPlayer target,
        MessageContent content
) implements NpcAction {}
