package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.GumpSent;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.LocalizedMessageContent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import com.github.mayconr.juoserver.infrastructure.i18n.ResourceBundleMessageLocalizer;
import com.github.mayconr.juoserver.network.packet.SendGumpDialog;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GumpLocalizationTest {
    @Test
    void resolvesBeforeLayoutAndPublishesTranslatedImmutableSnapshots() {
        var bus = mock(EventBus.class);
        var sent = new ArrayList<GumpSent>();
        doAnswer(invocation -> { sent.add(invocation.getArgument(0)); return null; })
                .when(bus).publish(any(GumpSent.class));
        var localizer = new ResourceBundleMessageLocalizer("i18n.gumps");
        var system = new DefaultGumpSystem(bus, new GamePlaySettings.Ui(), localizer);
        var row = new Row(5, List.of(
                new Label(MessageContent.localized("gump.greeting", Map.of("name", "Ana"))), new Label("next")));
        var column = new Column(5);
        column.add(row);
        column.add(new Button(4005, 4007, 1, MessageContent.localized("gump.button")));
        column.add(new HtmlGump(MessageContent.localized("gump.html"), 200, 40));
        column.add(FormField.stacked(new Label("Name"),
                new TextField(42, 100, 24, 0, MessageContent.localized("gump.initial"), true)));
        var ui = new DeclarativeGumpUI(new Page(0).add(new Panel(5054, 400, 200, column)));
        var player = new UOPlayer(new UOMobileData());
        player.setLocale(ClientLocale.PT_BR);
        system.send(player, ui, (context, response) -> {});
        var portuguese = sent.getFirst().builtGump();
        assertEquals(List.of("Olá, Ana!", "next", "Enviar", "<b>Bem-vindo</b>", "Name", "Valor inicial"), portuguese.texts);
        assertTrue(portuguese.layout.contains("{text " + (10 + "Olá, Ana!".length() * 7 + 5) + " 10 1152 1}"));

        player.setLocale(ClientLocale.EN_US);
        system.send(player, ui, (context, response) -> {});
        var english = sent.getLast().builtGump();
        assertEquals("Hello, Ana!", english.texts.getFirst());
        assertTrue(english.layout.contains("{text " + (10 + "Hello, Ana!".length() * 7 + 5) + " 10 1152 1}"));
        assertEquals("Olá, Ana!", portuguese.texts.getFirst());
        assertFalse(english.texts.contains("Enviar"));

        var buffer = Unpooled.buffer();
        try {
            new SendGumpDialog(player, sent.getFirst().gumpId(), 100, 100, portuguese.layout, portuguese.texts).writesTo(buffer);
            assertEquals(buffer.readableBytes(), buffer.getUnsignedShort(1));
        } finally { buffer.release(); }
    }

    @Test
    void localizedFactoriesPreserveTheExistingLiteralTextApi() {
        var ui = new DeclarativeGumpUI(DeclarativeGumpUI.Page(0,
                DeclarativeGumpUI.Label(""),
                DeclarativeGumpUI.Label(MessageContent.localized("gump.button")),
                DeclarativeGumpUI.Button(1, 2, 3, MessageContent.localized("gump.button")),
                DeclarativeGumpUI.HtmlGump(MessageContent.localized("gump.html"), 100, 40),
                DeclarativeGumpUI.TextField(42, 100, MessageContent.localized("gump.initial"))));
        var builder = new GumpBuilder();
        var localizer = new ResourceBundleMessageLocalizer("i18n.gumps");
        ui.render(builder, content -> localizer.localize(
                (LocalizedMessageContent) content, ClientLocale.PT_BR));
        assertEquals(List.of("", "Enviar", "Enviar", "<b>Bem-vindo</b>", "Valor inicial"), builder.buildTexts());
    }
}
