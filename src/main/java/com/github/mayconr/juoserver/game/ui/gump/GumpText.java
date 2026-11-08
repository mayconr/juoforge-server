package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;
import java.util.Objects;
import java.util.function.Function;

/** Keeps the original message so the same definition can be rendered in another language. */
final class GumpText {
    private final MessageContent source;
    private String value;

    GumpText(String text) {
        source = null;
        value = Objects.requireNonNull(text, "Gump text is required");
    }

    GumpText(MessageContent source) {
        this.source = Objects.requireNonNull(source, "Gump text is required");
        value = fallback(source);
    }

    static String fallback(MessageContent source) {
        return switch (source) {
            case PlainTextMessageContent plain -> plain.text();
            case LocalizedMessageContent localized -> localized.fallback();
        };
    }

    void resolve(Function<MessageContent, String> resolver) {
        if (source != null) value = Objects.requireNonNull(resolver.apply(source), "Resolved gump text is required");
    }

    String value() { return value; }
}
