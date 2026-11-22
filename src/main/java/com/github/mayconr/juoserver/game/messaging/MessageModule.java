package com.github.mayconr.juoserver.game.messaging;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;

import java.util.Optional;

public interface MessageModule extends WorldMessage {
    Optional<MessageStyleTemplate> findStyle(String name);
}
