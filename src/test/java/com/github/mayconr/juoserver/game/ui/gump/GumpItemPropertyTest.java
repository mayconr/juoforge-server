package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpItemPropertyTest {
    private String render(UIElement element) {
        var builder = new GumpBuilder().page(0);
        element.layout(new LayoutContext(0, 0, 200, 100));
        element.render(builder);
        return builder.buildLayout();
    }

    @Test
    void attachesObjectSerialImmediatelyAfterVisualControl() {
        int serial = 0x40000001;
        for (var control : List.of(new Image(111, 50, 50), new ItemIcon(4027),
                new Button(4005, 4007, 1), new ItemButton(4005, 4007, 1, 4027))) {
            var layout = render(control.itemProperty(serial));
            assertTrue(layout.contains("}\n{itemproperty 1073741825}"));
            assertFalse(layout.contains("{tooltip "));
        }
    }

    @Test
    void lastTooltipConfigurationWins() {
        var image = new Image(111, 50, 50);
        image.tooltip("text");
        image.itemProperty(123);
        assertTrue(render(image).contains("{itemproperty 123}"));
        assertFalse(render(image).contains("{tooltip "));
        image.tooltip(1042971, "changed");
        assertTrue(render(image).contains("{tooltip 1042971 @changed@}"));
        assertFalse(render(image).contains("{itemproperty "));
        image.itemProperty(456);
        image.tooltip("literal");
        assertTrue(render(image).contains("{tooltip 1042971 @literal@}"));
        assertFalse(render(image).contains("{itemproperty "));
    }

    @Test
    void panelTargetsItsBackgroundAndButtonAlsoTargetsItsCaption() {
        var panel = new Panel(3000, 100, 50, new Label("child")).itemProperty(123);
        assertTrue(render(panel).contains("{resizepic 0 0 3000 100 50}\n{itemproperty 123}\n{text "));
        var button = new ItemButton(4005, 4007, 1, 4027, "OK").itemProperty(123);
        assertEquals(2, render(button).split("itemproperty", -1).length - 1);
        assertThrows(IllegalStateException.class,
                () -> new Panel(100, 50, new Label("child")).itemProperty(123));
    }

    @Test
    void validatesSerialAndBuilderPageWithoutChangingCurrentTooltip() {
        var image = new Image(111, 50, 50);
        image.tooltip("before");
        for (int invalid : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class, () -> image.itemProperty(invalid));
            assertThrows(IllegalArgumentException.class, () -> new GumpBuilder().page(0).itemProperty(invalid));
        }
        assertTrue(render(image).contains("{tooltip 1042971 @before@}"));
        assertThrows(IllegalStateException.class, () -> new GumpBuilder().itemProperty(123));
        assertEquals("{page 0}\n{itemproperty 123}\n", new GumpBuilder().page(0).itemProperty(123).buildLayout());
    }
}
