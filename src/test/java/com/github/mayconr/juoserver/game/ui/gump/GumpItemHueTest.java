package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI.*;
import static org.junit.jupiter.api.Assertions.*;

class GumpItemHueTest {
    @Test
    void sendsHueWithoutChangingVisibleBoundsAlignmentOrTooltip() {
        var item = ItemIcon(0x0F61, 82, 46).visibleBounds(38, 0, 30, 30).hue(0x04E7);
        item.tooltip(1042971);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, ItemSlot(70, 50, item))).render(builder);
        assertTrue(builder.buildLayout().contains("{tilepichue -18 10 3937 1255}"));
        assertTrue(builder.buildLayout().contains("{tooltip 1042971}"));
    }

    @Test
    void zeroRestoresOriginalArtworkAndInvalidHuesAreRejected() {
        var item = ItemIcon(0x0F61).hue(1255).hue(0);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(Page(1, item)).render(builder);
        assertTrue(builder.buildLayout().contains("{tilepic  0 0 3937}"));
        assertThrows(IllegalArgumentException.class, () -> item.hue(-1));
        assertThrows(IllegalArgumentException.class, () -> item.hue(65536));
    }
}
