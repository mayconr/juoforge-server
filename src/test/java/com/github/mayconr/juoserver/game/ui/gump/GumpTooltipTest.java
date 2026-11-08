package com.github.mayconr.juoserver.game.ui.gump;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GumpTooltipTest {
    private String render(UIElement element) {
        var builder = new GumpBuilder().page(0);
        element.layout(new LayoutContext(0, 0, 200, 100));
        element.render(builder);
        return builder.buildLayout();
    }

    @Test
    void emitsTooltipsForAllSupportedVisualComponents() {
        var controls = List.of(
                new Label("label"), new Image(5054, 100, 50),
                new Button(4005, 4007, 1), new PageButton(4005, 4007, 2),
                new Checkbox(210, 211, 1, false), new Radio(210, 211, 2),
                new TextField(10, 100), new TextArea(20, 100, 60),
                new HtmlGump("html", 100, 60), new ItemIcon(3717),
                new ItemSlot(40, new ItemIcon(3717)), new Panel(5054, 100, 50, new Label("child")));
        for (var control : controls) {
            var layout = render(control.tooltip(1042971, "Hello world"));
            assertTrue(layout.contains("{tooltip 1042971 @Hello world@}"), control.getClass().getSimpleName());
        }
    }

    @Test
    void panelAndSlotTooltipsPrecedeTheirChildren() {
        var panel = new Panel(5054, 100, 50, new Label("child").tooltip(1042971, "child"));
        var layout = render(panel.tooltip(1042971, "panel"));
        assertTrue(layout.indexOf("{tooltip 1042971 @panel@}") < layout.indexOf("{text "));
        assertTrue(layout.indexOf("{tooltip 1042971 @child@}") > layout.indexOf("{text "));

        var slot = new ItemSlot(40, new ItemIcon(3717).tooltip(1042971, "item"));
        layout = render(slot.tooltip(1042971, "slot"));
        assertTrue(layout.indexOf("{tooltip 1042971 @slot@}") < layout.indexOf("{tilepic "));
    }

    @Test
    void captionedButtonAppliesTooltipToBothButtonAndCaption() {
        var layout = render(new Button(4005, 4007, 1, "OK").tooltip(1042971, "help"));
        assertEquals(2, layout.split("\\{tooltip ", -1).length - 1);
        int button = layout.indexOf("{button ");
        int firstTooltip = layout.indexOf("{tooltip ");
        int caption = layout.indexOf("{text ");
        assertTrue(button < firstTooltip && firstTooltip < caption);
        assertTrue(layout.lastIndexOf("{tooltip ") > caption);
    }

    @Test
    void componentsWithoutATooltipKeepTheirOriginalLayout() {
        assertFalse(render(new Label("plain")).contains("tooltip"));
        assertThrows(IllegalStateException.class,
                () -> new Panel(100, 50, new Label("plain")).tooltip(1042971));
    }

    @Test
    void supportsClilocOnlyAndQuotedArgumentsWithoutMutatingCallerData() {
        var builder = new GumpBuilder().page(0).tooltip(1042971).tooltip(1060658, "one, two", "three four");
        assertTrue(builder.buildLayout().contains("{tooltip 1042971}"));
        assertTrue(builder.buildLayout().contains("{tooltip 1060658 @one, two\tthree four@}"));
        var args = new String[]{"before"};
        var element = new Label("label").tooltip(1042971, args);
        args[0] = "after";
        assertTrue(render(element).contains("@before@"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"@", "{page 2}", "line\nbreak", "tab\tvalue", "\u00e1"})
    void rejectsArgumentsThatCannotBeRepresentedInTheAsciiLayout(String argument) {
        assertThrows(IllegalArgumentException.class, () -> new Label("label").tooltip(1042971, argument));
    }
}
