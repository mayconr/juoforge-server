package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpItemSlotAlignmentTest {
    @Test
    void centersVisiblePixelsInsteadOfTransparentArtMargins() {
        var icon = new ItemIcon(0x0F61, 82, 46).visibleBounds(38, 0, 30, 30);
        var slot = new ItemSlot(70, 50, icon);
        slot.layout(new LayoutContext(100, 100, 70, 50));
        var builder = new GumpBuilder().page(1);
        slot.render(builder);
        // Visible pixels start at (82 + 38, 110), centered in the 70x50 frame.
        assertTrue(builder.buildLayout().contains("{tilepic  82 110 3937}"));
    }

    @Test
    void centersOversizedArtWithEqualOverflowInsteadOfShiftingItRight() {
        var slot = new ItemSlot(70, 50, new ItemIcon(0x0F61, 82, 46));
        slot.layout(new LayoutContext(100, 100, 70, 50));
        var builder = new GumpBuilder().page(1);
        slot.render(builder);
        assertTrue(builder.buildLayout().contains("{resizepic 100 100 3000 70 50}"));
        assertTrue(builder.buildLayout().contains("{tilepic  94 102 3937}"));
    }

    @Test
    void centersSmallerArtWithoutApplyingAlignmentTwice() {
        var slot = new ItemSlot(3500, 100, 100, new ItemIcon(0x0F61, 82, 46).center());
        slot.layout(new LayoutContext(100, 100, 100, 100));
        var builder = new GumpBuilder().page(1);
        slot.render(builder);
        assertTrue(builder.buildLayout().contains("{resizepic 100 100 3500 100 100}"));
        assertTrue(builder.buildLayout().contains("{tilepic  109 127 3937}"));
    }
}
