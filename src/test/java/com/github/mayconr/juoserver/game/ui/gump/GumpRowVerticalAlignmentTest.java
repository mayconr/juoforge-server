package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.ItemIcon;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Label;
import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.Row;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpRowVerticalAlignmentTest {
    @Test
    void keepsChildrenAtTheTopByDefault() {
        var layout = layout(Row(6, ItemIcon(0x0F61), Label("test")), 0);

        assertTrue(layout.contains("{tilepic  0 0 3937}"), layout);
        assertTrue(layout.contains("{text 36 0 1152 0}"), layout);
    }

    @Test
    void centerAlignsChildrenOnBothAxes() {
        var layout = layout(Row(6, ItemIcon(0x0F61), Label("test")).center(), 200);

        assertTrue(layout.contains("{tilepic  68 0 3937}"), layout);
        assertTrue(layout.contains("{text 104 8 1152 0}"), layout);
    }

    /** Lays out the row in an area as tall as its item icon, as a column would assign it. */
    private static String layout(Row row, int width) {
        var builder = new GumpBuilder().page(0);
        row.layout(new LayoutContext(0, 0, width, row.getPreferredHeight()));
        row.render(builder);
        return builder.buildLayout();
    }
}
