package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GumpLabelHueTest {
    @Test
    void preservesTheDefaultHueAndAcceptsUoHueValues() {
        var builder = new GumpBuilder().page(0);
        var label = new Label("test");
        label.layout(new LayoutContext(10, 20, 100, 30));
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{text 10 20 1152 0}"));
        label.hue(0x04E7);
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{text 10 20 1255 1}"));
    }

    @Test
    void centersAfterResolvingLocalizedTextAndRetainsTheHue() {
        var label = new Label(MessageContent.localized("categories")).hue(0x04E7).horizontalCentered();
        label.resolveTexts(content -> "Categorias");
        label.layout(new LayoutContext(10, 20, 140, 30));
        var builder = new GumpBuilder().page(0);
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{text 45 20 1255 0}"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 65536})
    void rejectsInvalidHues(int hue) {
        assertThrows(IllegalArgumentException.class, () -> new Label("test").hue(hue));
    }
}
