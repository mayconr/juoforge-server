package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpClilocHtmlTest {
    @Test
    void emitsAllVariantsInTheirDistinctProtocolOrder() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0)
                .add(new HtmlGump(1051003, 200, 30, true, false))
                .add(new HtmlGump(1051003, 200, 30, false, true).color(0x7FFF))
                .add(new HtmlGump(1060658, 200, 30, true, true).arguments("Name", "Value").color(0x03E0)))
                .render(builder, message -> { throw new AssertionError("Client clilocs must not resolve on server"); });
        assertTrue(builder.buildLayout().contains("{xmfhtmlgump 0 0 200 30 1051003 1 0}"));
        assertTrue(builder.buildLayout().contains("{xmfhtmlgumpcolor 0 0 200 30 1051003 0 1 32767}"));
        assertTrue(builder.buildLayout().contains("{xmfhtmltok 0 0 200 30 1 1 992 1060658 @Name\tValue@}"));
        assertTrue(builder.buildTexts().isEmpty());
    }

    @Test
    void copiesArgumentsDefaultsTokenColorAndPreservesFitAndTooltip() {
        var args = new String[]{"Hello World"};
        var html = DeclarativeGumpUI.HtmlGump(1042971, 40, 20).arguments(args);
        html.tooltip("Help");
        args[0] = "changed";
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 100, html).fillChildren())).render(builder);
        assertTrue(builder.buildLayout().contains("{xmfhtmltok 10 10 180 80 0 0 0 1042971 @Hello World@}\n{tooltip 1042971 @Help@}"));
        assertEquals(40, html.getPreferredWidth());
        assertEquals(180, html.getWidth());
    }

    @Test
    void preservesServerResolvedHtmlAndTextTable() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0)
                .add(new HtmlGump("<b>literal</b>", 100, 20))
                .add(new HtmlGump(MessageContent.localized("key"), 100, 20)))
                .render(builder, message -> "translated");
        assertTrue(builder.buildLayout().contains("{htmlgump 0 0 100 20 0 0 0}"));
        assertTrue(builder.buildLayout().contains("{htmlgump 0 0 100 20 1 0 0}"));
        assertEquals(List.of("<b>literal</b>", "translated"), builder.buildTexts());
        assertThrows(IllegalStateException.class, () -> new HtmlGump("literal", 100, 20).color(100));
        assertThrows(IllegalStateException.class, () -> new HtmlGump("literal", 100, 20).arguments("arg"));
    }

    @Test
    void validatesClilocColorDimensionsAndLayoutArguments() {
        assertThrows(IllegalArgumentException.class, () -> new HtmlGump(0, 100, 20));
        assertThrows(IllegalArgumentException.class, () -> new HtmlGump(1, 0, 20));
        for (int color : new int[]{-1, 65536}) {
            assertThrows(IllegalArgumentException.class, () -> new HtmlGump(1, 100, 20).color(color));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0)
                    .xmfHtmlGumpColor(0, 0, 100, 20, 1, false, false, color));
        }
        for (String argument : new String[]{"@", "{page 1}", "line\nbreak", "tab\tvalue", "\u00e1"}) {
            assertThrows(IllegalArgumentException.class, () -> new HtmlGump(1, 100, 20).arguments(argument));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0)
                    .xmfHtmlTok(0, 0, 100, 20, 1, false, false, 0, argument));
        }
        assertThrows(NullPointerException.class, () -> new HtmlGump(1, 100, 20).arguments((String[]) null));
        assertThrows(NullPointerException.class, () -> new HtmlGump(1, 100, 20).arguments((String) null));
    }

    @Test
    void buildersRequireActivePage() {
        var builder = new GumpBuilder();
        assertThrows(IllegalStateException.class, () -> builder.xmfHtmlGump(0, 0, 100, 20, 1, false, false));
        assertThrows(IllegalStateException.class, () -> builder.xmfHtmlGumpColor(0, 0, 100, 20, 1, false, false, 0));
        assertThrows(IllegalStateException.class, () -> builder.xmfHtmlTok(0, 0, 100, 20, 1, false, false, 0, "text"));
    }
}
