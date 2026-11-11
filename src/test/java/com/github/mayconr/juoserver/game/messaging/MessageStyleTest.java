package com.github.mayconr.juoserver.game.messaging;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.MessageSent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageStyleTest {
    @Test
    void selectsExplicitStyleAndPreservesDefaultSystemStyle() {
        var events = mock(EventBus.class);
        var system = new MessageStyleTemplate("SYSTEM", TextType.SYSTEM, 946, 3);
        var enthusiastic = new MessageStyleTemplate("ENTHUSIASTIC", TextType.SYSTEM, 53, 3);
        var registry = new InMemoryTemplateRegistry<>(List.of(system, enthusiastic), MessageStyleTemplate::name);
        var messages = new MessageModuleImpl(events, registry);
        var player = mock(UOPlayer.class);
        var highlighted = MessageContent.localized("discovery");
        var ordinary = MessageContent.plain("Ordinary message");

        messages.send(player, highlighted, "ENTHUSIASTIC");
        messages.send(player, ordinary);

        verify(events).publish(new MessageSent(highlighted, null, player, enthusiastic));
        verify(events).publish(new MessageSent(ordinary, null, player, system));
    }
}
