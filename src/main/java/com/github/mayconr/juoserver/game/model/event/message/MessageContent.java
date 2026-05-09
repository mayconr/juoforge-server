package com.github.mayconr.juoserver.game.model.event.message;

import java.util.Map;

public sealed interface MessageContent permits PlainTextMessageContent, LocalizedMessageContent {

    static PlainTextMessageContent plain(String message) {
        return new PlainTextMessageContent(message);
    }

    static LocalizedMessageContent localized(String message, Map<String, Object> params) {
        return new LocalizedMessageContent(message, params, "!" + message + "!");
    }

    static LocalizedMessageContent localized(String message) {
        return new LocalizedMessageContent(message, Map.of(), "!" + message + "!");
    }
}
