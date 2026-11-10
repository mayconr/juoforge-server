package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpPanelFitTest {
    private String render(Panel panel) {
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(panel)).render(builder);
        return builder.buildLayout();
    }

    @Test
    void rootKeepsItsDimensionsAndFitsLabelToInnerArea() {
        var label = new Label("test").center();
        var panel = new Panel(3000, 200, 200, label).fillChildren();
        var layout = render(panel);
        assertEquals(200, panel.getWidth());
        assertEquals(200, panel.getHeight());
        assertEquals(180, label.getWidth());
        assertEquals(180, label.getHeight());
        assertTrue(layout.contains("{resizepic 0 0 3000 200 200}"));
        assertTrue(layout.contains("{text 86 93 1152 0}"));
    }

    @Test
    void fitsChildPanelAndResetsAssignedSizeOnSubsequentLayout() {
        var child = new Panel(3000, 50, 50, new Label("test"));
        var parent = new Panel(3000, 200, 200, child).fillChildren();
        assertTrue(render(parent).contains("{resizepic 10 10 3000 180 180}"));
        assertEquals(180, child.getWidth());
        assertEquals(50, child.getPreferredWidth());
        child.layout(new LayoutContext(0, 0, 500, 500));
        assertEquals(50, child.getWidth());
        assertEquals(50, child.getHeight());
        assertEquals(200, parent.getWidth());
    }

    @Test
    void fitTakesPrecedenceOverCenterInEitherOrder() {
        for (var panel : List.of(
                new Panel(3000, 200, 200, new Label("test")).fillChildren().center(),
                new Panel(3000, 200, 200, new Label("test")).center().fillChildren())) {
            assertTrue(render(panel).contains("{text 10 10 1152 0}"));
        }
    }

    @Test
    void fitsRowAndColumnWithoutEnablingTheirOwnFit() {
        var row = new Row(0, List.of(new Label("test")));
        render(new Panel(3000, 200, 200, row).fillChildren());
        assertEquals(180, row.getWidth());
        assertEquals(180, row.getHeight());
        var column = new Column(0).add(new Label("test"));
        render(new Panel(200, 200, column).fillChildren());
        assertEquals(200, column.getWidth());
        assertEquals(200, column.getHeight());
    }

    @Test
    void panelFitDoesNotRequestSpaceFromRowOrColumn() {
        var child = new Panel(40, 30, new Label("test")).fillChildren();
        var row = new Row(0, List.of(child)).fillChildren();
        row.layout(new LayoutContext(0, 0, 200, 200));
        assertEquals(40, child.getWidth());
        assertEquals(30, child.getHeight());
        var column = new Column(0).add(child).fillChildren();
        column.layout(new LayoutContext(0, 0, 200, 200));
        assertEquals(40, child.getWidth());
        assertEquals(30, child.getHeight());
    }

    @Test
    void fitsResizableContentAndPreservesNaturalDimensions() {
        var html = new HtmlGump("test", 40, 30);
        render(new Panel(3000, 200, 200, html).fillChildren());
        assertEquals(180, html.getWidth());
        assertEquals(180, html.getHeight());
        assertEquals(40, html.getPreferredWidth());
        html.layout(new LayoutContext(0, 0, 500, 500));
        assertEquals(40, html.getWidth());
        assertEquals(30, html.getHeight());
    }
}
