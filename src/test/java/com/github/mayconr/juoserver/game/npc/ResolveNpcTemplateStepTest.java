package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.resolve.ResolveNpcTemplateStep;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResolveNpcTemplateStepTest {
    private final TemplateRegistry<String, NpcTemplate> templates = mock(TemplateRegistry.class);
    private final MessageModule messages = mock(MessageModule.class);
    private final ResolveNpcTemplateStep step = new ResolveNpcTemplateStep(templates, messages);

    @Test
    void missingTemplateNotifiesRequestingPlayerAndStops() {
        var player = mock(UOPlayer.class);
        var context = context(new NpcRequester.Player(player));
        when(templates.get("missing")).thenReturn(List.of());

        assertTrue(step.execute(context).shouldStop());
        assertNull(context.getTemplate());
        verify(messages).send(player, MessageContent.localized(
                "createnp.template.notfound", Map.of("templateName", "missing")));
    }

    @Test
    void missingTemplateFromAsyncProcessStopsWithoutPlayerMessage() {
        var context = context(new NpcRequester.AsyncProcess("scheduled-spawn"));
        when(templates.get("missing")).thenReturn(List.of());

        assertTrue(step.execute(context).shouldStop());
        assertNull(context.getTemplate());
        verifyNoInteractions(messages);
    }

    @Test
    void existingTemplateResolvesForBothRequesterTypes() {
        var template = mock(NpcTemplate.class);
        when(templates.get("missing")).thenReturn(List.of(template));
        for (var requester : List.of(new NpcRequester.Player(mock(UOPlayer.class)),
                new NpcRequester.AsyncProcess("scheduled-spawn"))) {
            var context = context(requester);
            assertTrue(step.execute(context).shouldContinue());
            assertSame(template, context.getTemplate());
        }
        verifyNoInteractions(messages);
    }

    private NpcCreationContext context(NpcRequester requester) {
        return new NpcCreationContext(requester, "missing", mock(Location.class));
    }
}
