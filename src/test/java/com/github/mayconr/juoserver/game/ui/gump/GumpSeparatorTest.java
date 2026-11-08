package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.*;
import static org.junit.jupiter.api.Assertions.*;

class GumpSeparatorTest {
    @Test
    void emitsClientFieldOrderAndReservesHeightInColumn() {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, Panel(5054, 200, 100, true,
                Column(6, Separator(2101, 180, 4), Label("Below"))))).render(builder);
        assertTrue(builder.buildLayout().contains("{gumppictiled 10 10 180 4 2101}"));
        assertTrue(builder.buildLayout().contains("{text 10 20 1152 0}"));
        assertEquals(1, builder.buildTexts().size());
    }

    @Test
    void rejectsInvalidDimensionsAndRequiresPage() {
        assertThrows(IllegalArgumentException.class, () -> Separator(2101, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> Separator(-1, 100, 4));
        assertThrows(IllegalStateException.class, () -> new GumpBuilder().gumpPicTiled(0, 0, 100, 4, 2101));
    }
}
