package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GumpFitLayoutTest {
    @Test
    void singleChildFillsBothAxes() {
        var rowLabel = new Label("test");
        var columnLabel = new Label("test");
        var row = new Row(10, List.of(rowLabel)).fillChildren();
        var column = new Column(10).add(columnLabel).fillChildren();
        new Panel(3000, 200, 200, row).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        new Panel(3000, 200, 200, column).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(180, rowLabel.getWidth());
        assertEquals(180, rowLabel.getHeight());
        assertEquals(180, columnLabel.getWidth());
        assertEquals(180, columnLabel.getHeight());
    }

    @Test
    void rowDistributesAllChildrenRegardlessOfPreferredSize() {
        var first = new Panel(40, 20, new Label("first"));
        var second = new Panel(100, 50, new Label("second"));
        var third = new Panel(10, 10, new Label("third"));
        var row = new Row(10, List.of(first, second, third)).fillChildren();
        new Panel(3000, 200, 200, row).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(53, first.getWidth());
        assertEquals(53, second.getWidth());
        assertEquals(54, third.getWidth());
        assertEquals(180, first.getHeight());
        assertEquals(180, second.getHeight());
        assertEquals(180, third.getHeight());
    }

    @Test
    void columnDistributesAllChildrenAndRecomputesAfterResize() {
        var first = new Label("first");
        var second = new Panel(50, 80, new Label("second"));
        var column = new Column(10).add(first).add(second).fillChildren();
        new Panel(200, 111, column).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(50, first.getHeight());
        assertEquals(51, second.getHeight());
        assertEquals(200, first.getWidth());
        assertEquals(200, second.getWidth());
        new Panel(100, 30, column).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(10, first.getHeight());
        assertEquals(10, second.getHeight());
        assertEquals(100, first.getWidth());
        new Panel(100, 5, column).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(0, first.getHeight());
        assertEquals(0, second.getHeight());
    }

    @Test
    void nestedLayoutsDistributeTheirOwnChildren() {
        var topLeft = new Label("a");
        var bottomLeft = new Label("b");
        var topRight = new Label("c");
        var bottomRight = new Label("d");
        var left = new Column(10).add(topLeft).add(bottomLeft).fillChildren();
        var right = new Column(10).add(topRight).add(bottomRight).fillChildren();
        var row = new Row(10, List.of(left, right)).fillChildren();
        new Panel(210, 110, row).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        for (var label : List.of(topLeft, bottomLeft, topRight, bottomRight)) {
            assertEquals(100, label.getWidth());
            assertEquals(50, label.getHeight());
        }
    }

    @Test
    void handlesEmptyContainers() {
        var row = new Row(10, List.of()).fillChildren();
        var column = new Column(10).fillChildren();
        new Panel(100, 100, row).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        new Panel(100, 100, column).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        assertEquals(100, row.getWidth());
        assertEquals(100, column.getHeight());
    }

    @Test
    void preservesNaturalSizesWithoutFitAndRestoresChildrenOnRelayout() {
        var first = new Label("test");
        var second = new Label("test");
        var row = new Row(5, List.of(first, second));
        var column = new Column(5).add(row).add(new Label("test"));
        column.layout(new LayoutContext(0, 0, 200, 200));
        assertEquals(61, row.getWidth());
        assertEquals(14, row.getHeight());
        assertEquals(61, column.getWidth());
        assertEquals(33, column.getHeight());
        new Panel(200, 200, first).fillChildren().layout(new LayoutContext(0, 0, 0, 0));
        row.layout(new LayoutContext(0, 0, 200, 200));
        assertEquals(28, first.getWidth());
        assertEquals(14, first.getHeight());
    }
}
