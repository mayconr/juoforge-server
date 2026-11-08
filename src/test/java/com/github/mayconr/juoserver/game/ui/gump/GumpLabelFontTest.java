package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GumpLabelFontTest {
    @Test
    void retainsTheExistingClientDefaultByDefault() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Label("test"))).render(builder);
        assertTrue(builder.buildLayout().contains("{text 0 0 1152 0}"));
    }

    @Test
    void preservesHueTranslationAndCenteringWithAnExplicitFont() {
        var label = new Label(MessageContent.localized("categories")).hue(0x04E7).font(0).horizontalCentered();
        label.resolveTexts(content -> "Categorias");
        label.layout(new LayoutContext(10, 20, 140, 30));
        var builder = new GumpBuilder().page(0);
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{text 45 20 1255 0 0}"));
        assertEquals("Categorias", builder.buildTexts().getFirst());
        label.font(0xFF);
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{text 45 20 1255 1}"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 256})
    void rejectsInvalidFonts(int font) {
        assertThrows(IllegalArgumentException.class, () -> new Label("test").font(font));
        assertThrows(IllegalArgumentException.class,
                () -> new GumpBuilder().page(0).text(0, 0, 0, "test", font));
    }
}
