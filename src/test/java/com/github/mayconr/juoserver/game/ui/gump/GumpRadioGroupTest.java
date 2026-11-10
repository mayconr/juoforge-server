package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GumpRadioGroupTest {
    @Test
    void emitsIndependentSectionsAndPreservesSwitchIds() {
        var first = new RadioGroup(1, new Row(4, List.of(
                new Radio(208, 209, 10, true), new Radio(208, 209, 11))));
        var second = new RadioGroup(2, new Column(4, List.of(
                new Radio(208, 209, 20, true), new Radio(208, 209, 21))));
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(first).add(second).add(new Radio(208, 209, 30)))
                .render(builder);
        var tokens = Pattern.compile("\\{([^}]+)}").matcher(builder.buildLayout());
        int clientGroup = 0;
        var groups = new ArrayList<Integer>();
        var switches = new ArrayList<Integer>();
        while (tokens.find()) {
            var parts = tokens.group(1).split(" ");
            if (parts[0].equals("group") || parts[0].equals("endgroup")) clientGroup++;
            else if (parts[0].equals("radio")) {
                groups.add(clientGroup);
                switches.add(Integer.parseInt(parts[6]));
            }
        }
        assertEquals(List.of(1, 1, 3, 3, 4), groups);
        assertEquals(List.of(10, 11, 20, 21, 30), switches);
        assertNotEquals(groups.get(0), groups.get(2));
    }

    @Test
    void preservesFitAndLocalizationInsideGroup() {
        var content = new Column(0, List.of(new Label(MessageContent.localized("choice")))).fillChildren();
        var group = new RadioGroup(1, content);
        var builder = new GumpBuilder();
        new DeclarativeGumpUI(new Page(0).add(new Panel(3000, 200, 200, group).fillChildren()))
                .render(builder, message -> "Translated choice");
        assertEquals(180, group.getWidth());
        assertEquals(180, group.getHeight());
        assertEquals("Translated choice".length() * 7, group.getPreferredWidth());
        assertEquals(List.of("Translated choice"), builder.buildTexts());
        assertTrue(builder.buildLayout().contains("{group 1}"));
        assertTrue(builder.buildLayout().contains("{endgroup}"));
    }

    @Test
    void builderRequiresPageAndAcceptsGroupZero() {
        var builder = new GumpBuilder();
        assertThrows(IllegalStateException.class, () -> builder.group(1));
        assertThrows(IllegalStateException.class, builder::endGroup);
        assertEquals("{page 0}\n{group 0}\n{endgroup}\n", builder.page(0).group(0).endGroup().buildLayout());
        assertThrows(IllegalArgumentException.class, () -> builder.group(-1));
    }

    @Test
    void validatesDeclarativeGroupContent() {
        assertThrows(IllegalArgumentException.class, () -> new RadioGroup(-1, new Label("test")));
        assertThrows(NullPointerException.class, () -> new RadioGroup(1, null));
        assertThrows(IllegalArgumentException.class, () -> new RadioGroup(1, new Page(0)));
    }
}
