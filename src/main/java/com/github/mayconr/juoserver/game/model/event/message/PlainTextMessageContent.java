package com.github.mayconr.juoserver.game.model.event.message;

public record PlainTextMessageContent(String text) implements MessageContent {
    public PlainTextMessageContent {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be null or blank");
        }
    }

}
