package com.github.mayconr.juoserver.game.messaging;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.MessageSent;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MessagingModule implements WorldModule, MessagingOperations {

    public static final String NORMAL_STYLE_KEY = "NORMAL";
    public static final String SYSTEM_STYLE_KEY = "SYSTEM";
    public static final String BROADCAST_STYLE_KEY = "BROADCAST";
    public static final String SPEECH_MESSAGE_STYLE = "speech.messageStyle";
    private final EventBus eventBus;
    private final TemplateRegistry<String, MessageStyleTemplate> templateRegistry;

    @Override
    public void update(double delta) {}

    @Override
    public void send(UOPlayer player, String message) {
        if (LocalizationKey.isLocalizationKey(message)) {
            internalSend(player, MessageContent.localized(LocalizationKey.extractKey(message), Map.of()));
        } else {
            internalSend(player, MessageContent.plain(message));
        }
    }

    @Override
    public void send(UOPlayer player, MessageContent message) {
        internalSend(player, message);
    }

    private void internalSend(UOPlayer player, MessageContent message) {
        final var style = templateRegistry.get(SYSTEM_STYLE_KEY)
                .getFirst();
        eventBus.publish(new MessageSent(message, null, player, style));
    }

    @Override
    public void printTextAbove(UOObject source, MessageContent message) {
        internalPrintTextAbove(source, message, null);
    }

    @Override
    public void printTextAbove(UOObject source, MessageContent message, UOPlayer player) {
        internalPrintTextAbove(source, message, player);
    }

    private void internalPrintTextAbove(UOObject source, MessageContent message, UOPlayer player) {
        MessageStyleTemplate style;
        if (source instanceof UOMobile mobile) {
            final var styleName = mobile.persistentAttributes().getOrDefault(SPEECH_MESSAGE_STYLE, NORMAL_STYLE_KEY);
            style = templateRegistry.get(styleName).getFirst();
        } else {
            style = templateRegistry.get(SYSTEM_STYLE_KEY)
                    .getFirst();
        }

        eventBus.publish(new MessageSent(message, source, player, style));
    }

    @Override
    public void broadcast(MessageContent message) {
        final var style = templateRegistry.get(BROADCAST_STYLE_KEY)
                .getFirst();
        eventBus.publish(new MessageSent(message, null, null, style));
    }

}
