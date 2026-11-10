package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpImageHueTest {
    private String render(Image image) {
        image.layout(new LayoutContext(10, 20, 100, 100));
        var builder = new GumpBuilder().page(0);
        image.render(builder);
        return builder.buildLayout();
    }

    @Test
    void preservesDefaultAndEmitsBothColoredCommands() {
        assertTrue(render(new Image(111, 50, 50)).contains("{gumppic 10 20 111}"));
        assertTrue(render(new Image(111, 50, 50).hue(1152)).contains("{gumppichued 10 20 111 1152}"));
        assertTrue(render(new Image(111, 50, 50).partialHue(1152)).contains("{gumppicphued 10 20 111 1152}"));
    }

    @Test
    void lastModeWinsAndTooltipFollowsImage() {
        var image = new Image(111, 50, 50).partialHue(100).hue(200);
        image.tooltip(1042971);
        assertTrue(render(image).contains("{gumppichued 10 20 111 200}\n{tooltip 1042971}"));
        image.partialHue(300);
        assertTrue(render(image).contains("{gumppicphued 10 20 111 300}"));
        assertEquals(50, image.getWidth());
        assertEquals(50, image.getHeight());
    }

    @Test
    void validatesHueRangeAndRequiresPage() {
        for (int invalid : new int[]{-1, 65536}) {
            assertThrows(IllegalArgumentException.class, () -> new Image(111, 50, 50).hue(invalid));
            assertThrows(IllegalArgumentException.class, () -> new Image(111, 50, 50).partialHue(invalid));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0).gumpPicHued(0, 0, 111, invalid));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0).gumpPicPartialHued(0, 0, 111, invalid));
        }
        assertThrows(IllegalStateException.class, () -> new GumpBuilder().gumpPicHued(0, 0, 111, 1));
        assertThrows(IllegalStateException.class, () -> new GumpBuilder().gumpPicPartialHued(0, 0, 111, 1));
        assertTrue(render(new Image(111, 50, 50).hue(0)).contains("111 0}"));
        assertTrue(render(new Image(111, 50, 50).partialHue(65535)).contains("111 65535}"));
    }
}
