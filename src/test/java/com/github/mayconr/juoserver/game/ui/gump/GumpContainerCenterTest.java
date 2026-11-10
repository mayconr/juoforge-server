package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpContainerCenterTest {
    private String render(UIElement element, int width, int height) {
        element.layout(new LayoutContext(10, 20, width, height));
        var builder = new GumpBuilder().page(0);
        element.render(builder);
        return builder.buildLayout();
    }

    @Test
    void centersRowHorizontallyIncludingGaps() {
        var row = new Row(10, List.of(new Label("test"), new Label("test"))).center();
        var layout = render(row, 100, 100);
        assertTrue(layout.contains("{text 27 20 1152 0}"));
        assertTrue(layout.contains("{text 65 20 1152 1}"));
    }

    @Test
    void centersColumnVerticallyIncludingSpacing() {
        var column = new Column(10).add(new Label("test")).add(new Label("test")).center();
        var layout = render(column, 100, 100);
        assertTrue(layout.contains("{text 10 51 1152 0}"));
        assertTrue(layout.contains("{text 10 75 1152 1}"));
    }

    @Test
    void preservesDefaultAndDoesNotOffsetOversizedChildren() {
        assertTrue(render(new Row(0, List.of(new Label("test"))), 100, 100)
                .contains("{text 10 20 1152 0}"));
        assertTrue(render(new Column(0).add(new Label("test")), 100, 100)
                .contains("{text 10 20 1152 0}"));
        assertTrue(render(new Row(0, List.of(new Label("test"))).center(), 10, 10)
                .contains("{text 10 20 1152 0}"));
        assertTrue(render(new Column(0).add(new Label("test")).center(), 10, 10)
                .contains("{text 10 20 1152 0}"));
    }

    @Test
    void fitOverridesCenterRegardlessOfCallOrder() {
        for (var row : List.of(
                new Row(0, List.of(new Label("test"))).fillChildren().center(),
                new Row(0, List.of(new Label("test"))).center().fillChildren())) {
            var parent = new Panel(100, 100, row).fillChildren();
            assertTrue(render(parent, 0, 0).contains("{text 10 20 1152 0}"));
        }
        for (var column : List.of(
                new Column(0).add(new Label("test")).fillChildren().center(),
                new Column(0).add(new Label("test")).center().fillChildren())) {
            var parent = new Panel(100, 100, column).fillChildren();
            assertTrue(render(parent, 0, 0).contains("{text 10 20 1152 0}"));
        }
    }

    @Test
    void combinesWithParentFitAndNestedCentering() {
        var column = new Column(0).add(new Label("test")).center();
        var row = new Row(0, List.of(column)).center();
        var parent = new Panel(100, 100, row).fillChildren();
        assertTrue(render(parent, 0, 0).contains("{text 46 20 1152 0}"));
        var fullColumn = new Column(0).add(new Label("test")).center();
        assertTrue(render(new Panel(100, 100, fullColumn).fillChildren(), 0, 0)
                .contains("{text 10 63 1152 0}"));
    }
}
