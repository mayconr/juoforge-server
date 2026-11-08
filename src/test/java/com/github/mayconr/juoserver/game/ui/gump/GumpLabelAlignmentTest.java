package com.github.mayconr.juoserver.game.ui.gump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GumpLabelAlignmentTest {
    private String layout(Label label, int width, int height) {
        label.layout(new LayoutContext(10, 20, width, height));
        var builder = new GumpBuilder().page(0);
        label.render(builder);
        return builder.buildLayout();
    }

    @Test
    void centersVerticallyWithoutChangingHorizontalPosition() {
        assertTrue(layout(new Label("test").verticallyCentered(), 100, 50)
                .contains("{text 10 38 1152 0}"));
    }

    @Test
    void combinesHorizontalAndVerticalCentering() {
        assertTrue(layout(new Label("test").horizontalCentered().verticallyCentered(), 100, 50)
                .contains("{text 46 38 1152 0}"));
    }

    @Test
    void preservesDefaultAlignmentAndDoesNotOffsetOutsideSmallAreas() {
        assertTrue(layout(new Label("test"), 100, 50).contains("{text 10 20 1152 0}"));
        assertTrue(layout(new Label("test").horizontalCentered().verticallyCentered(), 10, 10)
                .contains("{text 10 20 1152 0}"));
    }
}
