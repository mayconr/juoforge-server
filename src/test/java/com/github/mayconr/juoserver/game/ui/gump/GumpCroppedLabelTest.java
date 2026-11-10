package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpCroppedLabelTest {
    @Test
    void preservesTextByDefaultAndCentersCroppedBox() {
        var label = new Label("A long text").cropped(40, 20).center().hue(100);
        label.layout(new LayoutContext(10, 20, 100, 80));
        var builder = new GumpBuilder().page(0);
        label.render(builder);
        assertTrue(builder.buildLayout().contains("{croppedtext 40 50 40 20 100 0}"));
        assertEquals(List.of("A long text"), builder.buildTexts());
        assertEquals(40, label.getWidth());
        assertEquals(20, label.getHeight());
        var plain = new Label("test");
        plain.layout(new LayoutContext(0, 0, 100, 80));
        plain.render(builder);
        assertTrue(builder.buildLayout().contains("{text 0 0 1152 1}"));
    }

    @Test
    void preservesLocalizationTooltipAndFitWithoutOffsettingOutsideParent() {
        var label = new Label(MessageContent.localized("long.text")).cropped(40, 20).center();
        label.tooltip("Full text");
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 100, 60, label).fillChildren()))
                .render(builder, message -> "Translated long text");
        assertTrue(builder.buildLayout().contains("{croppedtext 10 10 80 40 1152 0}\n{tooltip 1042971 @Full text@}"));
        assertEquals(List.of("Translated long text"), builder.buildTexts());
        assertEquals(40, label.getPreferredWidth());
        assertEquals(20, label.getPreferredHeight());
    }

    @Test
    void skipsEmptyAllocatedAreas() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(
                new Panel(3000, 20, 20, new Label("text").cropped(40, 20)).fillChildren())).render(builder);
        assertTrue(builder.buildTexts().isEmpty());
        assertEquals("{page 0}\n{resizepic 0 0 3000 20 20}\n", builder.buildLayout());
    }

    @Test
    void validatesDimensionsHueAndActivePage() {
        assertThrows(IllegalArgumentException.class, () -> new Label("test").cropped(0, 20));
        assertThrows(IllegalArgumentException.class, () -> new Label("test").cropped(20, -1));
        assertThrows(IllegalStateException.class, () -> new GumpBuilder().croppedText(0, 0, 40, 20, 0, "test"));
        var builder = new GumpBuilder().page(0);
        assertThrows(IllegalArgumentException.class, () -> builder.croppedText(0, 0, 0, 20, 0, "test"));
        assertThrows(IllegalArgumentException.class, () -> builder.croppedText(0, 0, 40, 20, 65536, "test"));
        assertTrue(builder.buildTexts().isEmpty());
    }
}
