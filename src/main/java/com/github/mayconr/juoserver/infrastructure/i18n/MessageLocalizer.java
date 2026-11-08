package com.github.mayconr.juoserver.infrastructure.i18n;

import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;

public interface MessageLocalizer {
    String localize(LocalizedMessageContent content, ClientLocale locale);
}
