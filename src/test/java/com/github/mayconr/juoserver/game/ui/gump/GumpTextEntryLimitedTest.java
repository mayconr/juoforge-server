package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpTextEntryLimitedTest {
    private String render(TextField field) {
        field.layout(new LayoutContext(10, 20, 100, 24));
        var builder = new GumpBuilder().page(0);
        field.render(builder);
        return builder.buildLayout();
    }

    @Test
    void preservesExistingConstructorsAndSupportsLimitedInput() {
        assertTrue(render(new TextField(42, 100, 24)).contains("{textentry 14 24 92 16 0 42 -1}"));
        assertTrue(render(new TextField(42, 100, 24, 20))
                .contains("{textentrylimited 14 24 92 16 0 42 -1 20}"));
        assertTrue(render(new TextField(42, 100, 24).maxCharacters(20))
                .contains("{textentrylimited 14 24 92 16 0 42 -1 20}"));
        assertTrue(render(new TextField(42, 100, 24, 5, "initial", false, 20))
                .contains("{textentrylimited 14 24 92 16 5 42 0 20}"));
    }

    @Test
    void preservesFitLocalizationAndTooltips() {
        var field = DeclarativeGumpUI.TextField(42, 100, 24, MessageContent.localized("initial"), 20);
        field.tooltip(1042971);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 100, field).fillChildren()))
                .render(builder, message -> "Ol?");
        assertTrue(builder.buildLayout().contains("{textentrylimited 14 14 172 72 0 42 0 20}"));
        assertTrue(builder.buildLayout().contains("{tooltip 1042971}"));
        assertEquals(List.of("Ol?"), builder.buildTexts());
    }

    @Test
    void validatesLimitsAndRequiresActivePage() {
        for (int invalid : new int[]{-1, 0, 240}) {
            assertThrows(IllegalArgumentException.class, () -> new TextField(1, 100, 24, invalid));
            assertThrows(IllegalArgumentException.class, () -> new TextField(1, 100, 24).maxCharacters(invalid));
            assertThrows(IllegalArgumentException.class,
                    () -> new GumpBuilder().page(0).textEntryLimited(0, 0, 100, 24, 0, 1, -1, invalid));
        }
        assertThrows(IllegalStateException.class,
                () -> new GumpBuilder().textEntryLimited(0, 0, 100, 24, 0, 1, -1, 20));
        assertTrue(render(new TextField(1, 100, 24, 1)).contains(" -1 1}"));
        assertTrue(render(new TextField(1, 100, 24, 239)).contains(" -1 239}"));
    }
}
