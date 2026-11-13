package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpAlignmentTest {
    private String layout(UIElement element) {
        element.layout(new LayoutContext(100, 50, 200, 100));
        var builder = new GumpBuilder();
        builder.page(0);
        element.render(builder);
        return builder.buildLayout();
    }

    @Test
    void labelsPreserveVerticalCenteringAndLastHorizontalChoiceWins() {
        var label = new Label("A").center();
        assertTrue(layout(label.right()).contains("{text 293 93 "));
        assertTrue(layout(label.left()).contains("{text 100 93 "));
        assertTrue(layout(label.center()).contains("{text 196 93 "));
    }

    @Test
    void iconAlignmentUsesVisibleBoundsAndPreservesOffsets() {
        var icon = new ItemIcon(0x0F61, 82, 46).visibleBounds(38, 0, 30, 30).offset(2, 3).center();
        assertTrue(layout(icon.right()).contains("{tilepic  234 88 "));
        assertTrue(layout(icon.left()).contains("{tilepic  64 88 "));
    }

    @Test
    void panelsAlignNaturalContentAndFillChildrenTakesPrecedence() {
        var panel = new Panel(200, 100, new Label("A")).center();
        assertTrue(layout(panel.right()).contains("{text 293 93 "));
        assertTrue(layout(panel.left()).contains("{text 100 93 "));
        assertTrue(layout(panel.fillChildren().right()).contains("{text 100 50 "));
    }

    @Test
    void rowsAlignTheirChildrenAsAGroup() {
        var row = new Row(10, List.of(new Label("A"), new Label("B")));
        assertTrue(layout(row.right()).contains("{text 276 50 "));
        assertTrue(layout(row.left()).contains("{text 100 50 "));
        assertTrue(layout(row.center()).contains("{text 188 93 "));
        assertTrue(layout(row.right()).contains("{text 276 93 "));
    }

    @Test
    void columnsAlignEachChildWhileKeepingVerticalCentering() {
        var column = new Column(10, List.of(new Label("A"), new Label("BBB"))).center();
        String right = layout(column.right());
        assertTrue(right.contains("{text 293 81 "));
        assertTrue(right.contains("{text 279 105 "));
        assertTrue(layout(column.left()).contains("{text 100 81 "));
    }
    @Test
    void topAndDownPreserveHorizontalAlignment() {
        var label = new Label("A").right();
        assertTrue(layout(label.down()).contains("{text 293 136 "));
        assertTrue(layout(label.top()).contains("{text 293 50 "));
        var icon = new ItemIcon(1, 30, 30).right();
        assertTrue(layout(icon.down()).contains("{tilepic  270 120 "));
        assertTrue(layout(icon.top()).contains("{tilepic  270 50 "));
        var panel = new Panel(200, 100, new Label("A")).right();
        assertTrue(layout(panel.down()).contains("{text 293 136 "));
        assertTrue(layout(panel.top()).contains("{text 293 50 "));
    }

    @Test
    void columnMovesTheGroupAndRowMovesEachChildVertically() {
        var column = new Column(10, List.of(new Label("A"), new Label("BBB"))).right();
        String bottom = layout(column.down());
        assertTrue(bottom.contains("{text 293 112 "));
        assertTrue(bottom.contains("{text 279 136 "));
        assertTrue(layout(column.top()).contains("{text 293 50 "));
        var row = new Row(10, List.of(new Label("A"), new ItemIcon(1, 30, 30))).right();
        bottom = layout(row.down());
        assertTrue(bottom.contains("{text 253 136 "));
        assertTrue(bottom.contains("{tilepic  270 120 "));
        assertTrue(layout(row.top()).contains("{text 253 50 "));
    }

    @Test
    void filledContainersIgnoreBottomAlignment() {
        var panel = new Panel(200, 100, new Label("A")).fillChildren().down();
        assertTrue(layout(panel).contains("{text 100 50 "));
        var row = new Row(0, List.of(new Label("A"))).fillChildren().down();
        assertTrue(layout(row).contains("{text 100 50 "));
        var column = new Column(0, List.of(new Label("A"))).fillChildren().down();
        assertTrue(layout(column).contains("{text 100 50 "));
    }
}
