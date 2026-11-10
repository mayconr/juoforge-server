package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpPanelCenterTest {
    private String render(Panel panel) {
        panel.layout(new LayoutContext(0, 0, 0, 0));
        var builder = new GumpBuilder().page(0);
        panel.render(builder);
        return builder.buildLayout();
    }

    @Test
    void centersLabelWithoutApplyingAlignmentTwice() {
        assertTrue(render(new Panel(3000, 200, 200,
                new Label("test").horizontalCentered().verticallyCentered()).center())
                .contains("{text 86 93 1152 0}"));
        assertTrue(render(new Panel(200, 200, new Label("test")).center())
                .contains("{text 86 93 1152 0}"));
    }

    @Test
    void centersNaturalBoundsOfContainersThatFitTheirChildren() {
        var column = new Column(10).add(new Label("test")).add(new Label("test"))
                .fillChildren();
        var row = new Row(0, List.of(column)).fillChildren();
        assertTrue(render(new Panel(3000, 200, 200, row).center())
                .contains("{text 86 81 1152 0}"));
        assertEquals(28, row.getWidth());
        assertEquals(38, row.getHeight());
        assertEquals(28, column.getWidth());
    }

    @Test
    void preservesDefaultAndAnchorsOversizedContentAtInnerOrigin() {
        assertTrue(render(new Panel(3000, 200, 200, new Label("test")))
                .contains("{text 10 10 1152 0}"));
        assertTrue(render(new Panel(3000, 20, 20, new Label("test")).center())
                .contains("{text 10 10 1152 0}"));
    }

    @Test
    void rejectsContentWithoutDimensions() {
        assertThrows(IllegalStateException.class, () -> new Panel(200, 200, new Page(0)).center());
    }
}
