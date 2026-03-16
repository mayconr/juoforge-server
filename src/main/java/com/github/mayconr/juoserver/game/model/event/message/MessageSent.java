package com.github.mayconr.juoserver.game.model.event.message;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MessageSent(MessageContent messageContent, UOObject messageSource, UOPlayer messageTarget, MessageStyleTemplate messageStyle) implements GameEvent {

}
